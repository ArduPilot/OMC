/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package eu.mavinci.core.crypto;

import eu.mavinci.core.helper.HexStringHelper;
import eu.mavinci.core.helper.HexStringHelper;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CryptoHelper {
    private String pubKeyDerHexString;
    private final String cryptKey;
    private final String iv;

    /**
     * Provides methods for data decryption and signature verification.
     *
     * @param pubKeyDerHexString Public key for signature verification (binary contents of pem file as hex string).
     * @param cryptKey Encryption key (16 chars for AES-128).
     * @param iv Initialization vector (16 chars) for encryption.
     */
    public CryptoHelper(String pubKeyDerHexString, String cryptKey, String iv) {
        this.pubKeyDerHexString = pubKeyDerHexString;
        this.cryptKey = cryptKey;
        this.iv = iv;
    }

    /**
     * Decrypts the given data with the encyrption key and iv, unzips the data and signature, and verfies the signature
     * with the public key.
     *
     * @param encryptedData Byte array of encrypted data.
     * @return The decrypted data (only if decryption and verification succeeded).
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public byte[] decryptDataAndCheckSignature(byte[] encData) throws IOException, GeneralSecurityException {
        byte[] zipData = decrypt(encData);
        byte[] data = unzipDataAndCheckSignature(zipData);

        return data;
    }

    /**
     * Decrypts the given data file with the encyrption key and iv, unzips the data and signature, and verfies the
     * signature with the public key.
     *
     * @param encFileName The path / file name of the encrypted input file.
     * @return The decrypted data (only if decryption and verification succeeded).
     */
    public byte[] decryptDataAndCheckSignature(File encFile) throws IOException, GeneralSecurityException {
        return decryptDataAndCheckSignature(Files.readAllBytes(encFile.toPath()));
    }

    public static class ZipData {
        public String name;
        public byte[] data;

        public ZipData(String name, byte[] data) {
            this.name = name;
            this.data = data;
        }

        public static ZipData readNextZipData(ZipInputStream zis) throws IOException {
            ZipEntry zipEntry = zis.getNextEntry();
            if (zipEntry == null) {
                return null;
            }

            int initSize = 1024;
            if (zipEntry.getSize() >= 0) {
                initSize = (int)zipEntry.getSize();
            }

            int byteCount;
            byte buffer[] = new byte[initSize];
            ByteArrayOutputStream baos = new ByteArrayOutputStream(initSize);

            while ((byteCount = zis.read(buffer, 0, initSize)) != -1) {
                baos.write(buffer, 0, byteCount);
            }

            return new ZipData(zipEntry.getName(), baos.toByteArray());
        }
    }

    public static byte[] signWithPrivateDerKeyAndZip(byte[] data, String privKeyDerHexString)
            throws GeneralSecurityException, IOException {
        byte[] privateKeyBytes = HexStringHelper.hexStringToByteArray(privKeyDerHexString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        PrivateKey privKey = keyFactory.generatePrivate(keySpec);

        Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
        sig.initSign(privKey);
        sig.update(data);
        byte[] signatureBytes = sig.sign();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(baos));
        zos.putNextEntry(new ZipEntry("data"));
        zos.write(data);
        zos.closeEntry();
        zos.putNextEntry(new ZipEntry("data.signature"));
        zos.write(signatureBytes);
        zos.closeEntry();
        zos.close();

        return baos.toByteArray();
    }

    public static byte[] encryptWithKeyAndIv(byte[] data, byte[] key, byte[] iv) throws GeneralSecurityException {
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

        byte[] encrypted = cipher.doFinal(data);

        return encrypted;
    }

    private byte[] unzipDataAndCheckSignature(byte[] zipDataBytes) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new ByteArrayInputStream(zipDataBytes)))) {
            ZipData[] zipData = new ZipData[2];
            ZipData signatureZipData = null;
            ZipData dataZipData = null;

            for (int i = 0; i < zipData.length; i++) {
                zipData[i] = ZipData.readNextZipData(zis);
                if (zipData[i] == null) {
                    throw new CryptoHelperException("Invalid file contents: Entries missing");
                }

                if (zipData[i].name.endsWith(".signature")) {
                    signatureZipData = zipData[i];
                } else {
                    dataZipData = zipData[i];
                }
            }

            if (ZipData.readNextZipData(zis) != null) {
                throw new CryptoHelperException("Invalid file contents: Too many entries");
            }

            if (signatureZipData == null) {
                throw new CryptoHelperException("Invalid file contents: No signature entry found");
            }

            if (dataZipData == null) {
                throw new CryptoHelperException("Invalid file contents: No data entry found");
            }

            // TODO: obfuscate
            try {
                if (!verifySignature(dataZipData.data, signatureZipData.data)) {
                    throw new CryptoHelperException("Invalid file contents: Signature verification failed");
                } else {
                    return dataZipData.data;
                }
            } catch (GeneralSecurityException ex) {
                throw new CryptoHelperException("Error verifying signature: " + ex.getMessage());
            }
        }
    }

    private boolean verifySignature(byte[] data, byte[] signature) throws GeneralSecurityException, IOException {
        // doc: See
        // https://docs.oracle.com/javase/tutorial/security/apisign/index.html

        // byte[] publicKeyBytes =
        // Files.readAllBytes(Paths.get("./src/eu/mavinci/crypto/test/dsa_pub.der"));
        byte[] publicKeyBytes = HexStringHelper.hexStringToByteArray(pubKeyDerHexString);

        PublicKey pubKey =
            (PublicKey)KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
        sig.initVerify(pubKey);
        sig.update(data);
        boolean verifies = sig.verify(signature);

        return verifies;
    }

    private byte[] decrypt(byte[] data) throws GeneralSecurityException {
        IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
        SecretKeySpec keyspec = new SecretKeySpec(cryptKey.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // In case of error: java.security.InvalidKeyException: Illegal key
        // size:
        // AES-256 (32 character key) is only allowed with
        // "Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction
        // Policy Files"
        // installed. Use AES-128 (16 character key) otherwise.
        cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

        byte[] decrypted = cipher.doFinal(data);

        return decrypted;
    }
}
