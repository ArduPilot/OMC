/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package gov.nasa.worldwind.render;

import com.jogamp.common.nio.PointerBuffer;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL3ES3;
import com.jogamp.opengl.GL3bc;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GL4ES3;
import com.jogamp.opengl.GL4bc;
import com.jogamp.opengl.GLArrayData;
import com.jogamp.opengl.GLBufferStorage;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLES1;
import com.jogamp.opengl.GLES2;
import com.jogamp.opengl.GLES3;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLUniformData;

public class DebugGL implements GL2 {

    public static class GLFunc {
        public static final int glClearIndex = 0;
        public static final int glIndexMask = 1;
        public static final int glLineStipple = 2;
        public static final int glPolygonStipple = 3;
        public static final int glGetPolygonStipple = 4;
        public static final int glEdgeFlag = 5;
        public static final int glEdgeFlagv = 6;
        public static final int glClipPlane = 7;
        public static final int glGetClipPlane = 8;
        public static final int glPushAttrib = 9;
        public static final int glPopAttrib = 10;
        public static final int glRenderMode = 11;
        public static final int glClearAccum = 12;
        public static final int glAccum = 13;
        public static final int glLoadMatrixd = 14;
        public static final int glMultMatrixd = 15;
        public static final int glRotated = 16;
        public static final int glScaled = 17;
        public static final int glTranslated = 18;
        public static final int glIsList = 19;
        public static final int glDeleteLists = 20;
        public static final int glGenLists = 21;
        public static final int glNewList = 22;
        public static final int glEndList = 23;
        public static final int glCallList = 24;
        public static final int glCallLists = 25;
        public static final int glListBase = 26;
        public static final int glBegin = 27;
        public static final int glEnd = 28;
        public static final int glVertex2d = 29;
        public static final int glVertex2f = 30;
        public static final int glVertex2i = 31;
        public static final int glVertex2s = 32;
        public static final int glVertex3d = 33;
        public static final int glVertex3f = 34;
        public static final int glVertex3i = 35;
        public static final int glVertex3s = 36;
        public static final int glVertex4d = 37;
        public static final int glVertex4f = 38;
        public static final int glVertex4i = 39;
        public static final int glVertex4s = 40;
        public static final int glVertex2dv = 41;
        public static final int glVertex2fv = 42;
        public static final int glVertex2iv = 43;
        public static final int glVertex2sv = 44;
        public static final int glVertex3dv = 45;
        public static final int glVertex3fv = 46;
        public static final int glVertex3iv = 47;
        public static final int glVertex3sv = 48;
        public static final int glVertex4dv = 49;
        public static final int glVertex4fv = 50;
        public static final int glVertex4iv = 51;
        public static final int glVertex4sv = 52;
        public static final int glNormal3b = 53;
        public static final int glNormal3d = 54;
        public static final int glNormal3i = 55;
        public static final int glNormal3s = 56;
        public static final int glNormal3bv = 57;
        public static final int glNormal3dv = 58;
        public static final int glNormal3fv = 59;
        public static final int glNormal3iv = 60;
        public static final int glNormal3sv = 61;
        public static final int glIndexd = 62;
        public static final int glIndexf = 63;
        public static final int glIndexi = 64;
        public static final int glIndexs = 65;
        public static final int glIndexdv = 66;
        public static final int glIndexfv = 67;
        public static final int glIndexiv = 68;
        public static final int glIndexsv = 69;
        public static final int glColor3b = 70;
        public static final int glColor3d = 71;
        public static final int glColor3f = 72;
        public static final int glColor3i = 73;
        public static final int glColor3s = 74;
        public static final int glColor3ub = 75;
        public static final int glColor3ui = 76;
        public static final int glColor3us = 77;
        public static final int glColor4b = 78;
        public static final int glColor4d = 79;
        public static final int glColor4i = 80;
        public static final int glColor4s = 81;
        public static final int glColor4ui = 82;
        public static final int glColor4us = 83;
        public static final int glColor3bv = 84;
        public static final int glColor3dv = 85;
        public static final int glColor3fv = 86;
        public static final int glColor3iv = 87;
        public static final int glColor3sv = 88;
        public static final int glColor3ubv = 89;
        public static final int glColor3uiv = 90;
        public static final int glColor3usv = 91;
        public static final int glColor4bv = 92;
        public static final int glColor4dv = 93;
        public static final int glColor4fv = 94;
        public static final int glColor4iv = 95;
        public static final int glColor4sv = 96;
        public static final int glColor4ubv = 97;
        public static final int glColor4uiv = 98;
        public static final int glColor4usv = 99;
        public static final int glTexCoord1d = 100;
        public static final int glTexCoord1f = 101;
        public static final int glTexCoord1i = 102;
        public static final int glTexCoord1s = 103;
        public static final int glTexCoord2d = 104;
        public static final int glTexCoord2f = 105;
        public static final int glTexCoord2i = 106;
        public static final int glTexCoord2s = 107;
        public static final int glTexCoord3d = 108;
        public static final int glTexCoord3f = 109;
        public static final int glTexCoord3i = 110;
        public static final int glTexCoord3s = 111;
        public static final int glTexCoord4d = 112;
        public static final int glTexCoord4f = 113;
        public static final int glTexCoord4i = 114;
        public static final int glTexCoord4s = 115;
        public static final int glTexCoord1dv = 116;
        public static final int glTexCoord1fv = 117;
        public static final int glTexCoord1iv = 118;
        public static final int glTexCoord1sv = 119;
        public static final int glTexCoord2dv = 120;
        public static final int glTexCoord2fv = 121;
        public static final int glTexCoord2iv = 122;
        public static final int glTexCoord2sv = 123;
        public static final int glTexCoord3dv = 124;
        public static final int glTexCoord3fv = 125;
        public static final int glTexCoord3iv = 126;
        public static final int glTexCoord3sv = 127;
        public static final int glTexCoord4dv = 128;
        public static final int glTexCoord4fv = 129;
        public static final int glTexCoord4iv = 130;
        public static final int glTexCoord4sv = 131;
        public static final int glRasterPos2d = 132;
        public static final int glRasterPos2f = 133;
        public static final int glRasterPos2i = 134;
        public static final int glRasterPos2s = 135;
        public static final int glRasterPos3d = 136;
        public static final int glRasterPos3f = 137;
        public static final int glRasterPos3i = 138;
        public static final int glRasterPos3s = 139;
        public static final int glRasterPos4d = 140;
        public static final int glRasterPos4f = 141;
        public static final int glRasterPos4i = 142;
        public static final int glRasterPos4s = 143;
        public static final int glRasterPos2dv = 144;
        public static final int glRasterPos2fv = 145;
        public static final int glRasterPos2iv = 146;
        public static final int glRasterPos2sv = 147;
        public static final int glRasterPos3dv = 148;
        public static final int glRasterPos3fv = 149;
        public static final int glRasterPos3iv = 150;
        public static final int glRasterPos3sv = 151;
        public static final int glRasterPos4dv = 152;
        public static final int glRasterPos4fv = 153;
        public static final int glRasterPos4iv = 154;
        public static final int glRasterPos4sv = 155;
        public static final int glRectd = 156;
        public static final int glRectf = 157;
        public static final int glRecti = 158;
        public static final int glRects = 159;
        public static final int glRectdv = 160;
        public static final int glRectfv = 161;
        public static final int glRectiv = 162;
        public static final int glRectsv = 163;
        public static final int glLighti = 164;
        public static final int glLightiv = 165;
        public static final int glGetLightiv = 166;
        public static final int glLightModeli = 167;
        public static final int glLightModeliv = 168;
        public static final int glMateriali = 169;
        public static final int glMaterialiv = 170;
        public static final int glGetMaterialiv = 171;
        public static final int glColorMaterial = 172;
        public static final int glPixelZoom = 173;
        public static final int glPixelTransferf = 174;
        public static final int glPixelTransferi = 175;
        public static final int glPixelMapfv = 176;
        public static final int glPixelMapuiv = 177;
        public static final int glPixelMapusv = 178;
        public static final int glGetPixelMapfv = 179;
        public static final int glGetPixelMapuiv = 180;
        public static final int glGetPixelMapusv = 181;
        public static final int glBitmap = 182;
        public static final int glDrawPixels = 183;
        public static final int glCopyPixels = 184;
        public static final int glTexGend = 185;
        public static final int glTexGenf = 186;
        public static final int glTexGeni = 187;
        public static final int glTexGendv = 188;
        public static final int glTexGenfv = 189;
        public static final int glTexGeniv = 190;
        public static final int glGetTexGendv = 191;
        public static final int glGetTexGenfv = 192;
        public static final int glGetTexGeniv = 193;
        public static final int glMap1d = 194;
        public static final int glMap1f = 195;
        public static final int glMap2d = 196;
        public static final int glMap2f = 197;
        public static final int glGetMapdv = 198;
        public static final int glGetMapfv = 199;
        public static final int glGetMapiv = 200;
        public static final int glEvalCoord1d = 201;
        public static final int glEvalCoord1f = 202;
        public static final int glEvalCoord1dv = 203;
        public static final int glEvalCoord1fv = 204;
        public static final int glEvalCoord2d = 205;
        public static final int glEvalCoord2f = 206;
        public static final int glEvalCoord2dv = 207;
        public static final int glEvalCoord2fv = 208;
        public static final int glMapGrid1d = 209;
        public static final int glMapGrid1f = 210;
        public static final int glMapGrid2d = 211;
        public static final int glMapGrid2f = 212;
        public static final int glEvalPoint1 = 213;
        public static final int glEvalPoint2 = 214;
        public static final int glEvalMesh1 = 215;
        public static final int glEvalMesh2 = 216;
        public static final int glFogi = 217;
        public static final int glFogiv = 218;
        public static final int glFeedbackBuffer = 219;
        public static final int glPassThrough = 220;
        public static final int glSelectBuffer = 221;
        public static final int glInitNames = 222;
        public static final int glLoadName = 223;
        public static final int glPushName = 224;
        public static final int glPopName = 225;
        public static final int glIndexub = 226;
        public static final int glIndexubv = 227;
        public static final int glPushClientAttrib = 228;
        public static final int glPopClientAttrib = 229;
        public static final int glIndexPointer = 230;
        public static final int glEdgeFlagPointer = 231;
        public static final int glArrayElement = 232;
        public static final int glInterleavedArrays = 233;
        public static final int glPrioritizeTextures = 234;
        public static final int glAreTexturesResident = 235;
        public static final int glMultiTexCoord1d = 236;
        public static final int glMultiTexCoord1dv = 237;
        public static final int glMultiTexCoord1f = 238;
        public static final int glMultiTexCoord1fv = 239;
        public static final int glMultiTexCoord1i = 240;
        public static final int glMultiTexCoord1iv = 241;
        public static final int glMultiTexCoord1s = 242;
        public static final int glMultiTexCoord1sv = 243;
        public static final int glMultiTexCoord2d = 244;
        public static final int glMultiTexCoord2dv = 245;
        public static final int glMultiTexCoord2f = 246;
        public static final int glMultiTexCoord2fv = 247;
        public static final int glMultiTexCoord2i = 248;
        public static final int glMultiTexCoord2iv = 249;
        public static final int glMultiTexCoord2s = 250;
        public static final int glMultiTexCoord2sv = 251;
        public static final int glMultiTexCoord3d = 252;
        public static final int glMultiTexCoord3dv = 253;
        public static final int glMultiTexCoord3f = 254;
        public static final int glMultiTexCoord3fv = 255;
        public static final int glMultiTexCoord3i = 256;
        public static final int glMultiTexCoord3iv = 257;
        public static final int glMultiTexCoord3s = 258;
        public static final int glMultiTexCoord3sv = 259;
        public static final int glMultiTexCoord4d = 260;
        public static final int glMultiTexCoord4dv = 261;
        public static final int glMultiTexCoord4fv = 262;
        public static final int glMultiTexCoord4i = 263;
        public static final int glMultiTexCoord4iv = 264;
        public static final int glMultiTexCoord4s = 265;
        public static final int glMultiTexCoord4sv = 266;
        public static final int glLoadTransposeMatrixf = 267;
        public static final int glLoadTransposeMatrixd = 268;
        public static final int glMultTransposeMatrixf = 269;
        public static final int glMultTransposeMatrixd = 270;
        public static final int glFogCoordf = 271;
        public static final int glFogCoordfv = 272;
        public static final int glFogCoordd = 273;
        public static final int glFogCoorddv = 274;
        public static final int glFogCoordPointer = 275;
        public static final int glSecondaryColor3b = 276;
        public static final int glSecondaryColor3bv = 277;
        public static final int glSecondaryColor3d = 278;
        public static final int glSecondaryColor3dv = 279;
        public static final int glSecondaryColor3f = 280;
        public static final int glSecondaryColor3fv = 281;
        public static final int glSecondaryColor3i = 282;
        public static final int glSecondaryColor3iv = 283;
        public static final int glSecondaryColor3s = 284;
        public static final int glSecondaryColor3sv = 285;
        public static final int glSecondaryColor3ub = 286;
        public static final int glSecondaryColor3ubv = 287;
        public static final int glSecondaryColor3ui = 288;
        public static final int glSecondaryColor3uiv = 289;
        public static final int glSecondaryColor3us = 290;
        public static final int glSecondaryColor3usv = 291;
        public static final int glSecondaryColorPointer = 292;
        public static final int glWindowPos2d = 293;
        public static final int glWindowPos2dv = 294;
        public static final int glWindowPos2f = 295;
        public static final int glWindowPos2fv = 296;
        public static final int glWindowPos2i = 297;
        public static final int glWindowPos2iv = 298;
        public static final int glWindowPos2s = 299;
        public static final int glWindowPos2sv = 300;
        public static final int glWindowPos3d = 301;
        public static final int glWindowPos3dv = 302;
        public static final int glWindowPos3f = 303;
        public static final int glWindowPos3fv = 304;
        public static final int glWindowPos3i = 305;
        public static final int glWindowPos3iv = 306;
        public static final int glWindowPos3s = 307;
        public static final int glWindowPos3sv = 308;
        public static final int glClearNamedBufferData = 309;
        public static final int glClearNamedBufferSubData = 310;
        public static final int glNamedFramebufferParameteri = 311;
        public static final int glGetNamedFramebufferParameteriv = 312;
        public static final int glGetnMapdv = 313;
        public static final int glGetnMapfv = 314;
        public static final int glGetnMapiv = 315;
        public static final int glGetnPixelMapfv = 316;
        public static final int glGetnPixelMapuiv = 317;
        public static final int glGetnPixelMapusv = 318;
        public static final int glGetnPolygonStipple = 319;
        public static final int glGetnColorTable = 320;
        public static final int glGetnConvolutionFilter = 321;
        public static final int glGetnSeparableFilter = 322;
        public static final int glGetnHistogram = 323;
        public static final int glGetnMinmax = 324;
        public static final int glColorTable = 325;
        public static final int glColorTableParameterfv = 326;
        public static final int glColorTableParameteriv = 327;
        public static final int glCopyColorTable = 328;
        public static final int glGetColorTable = 329;
        public static final int glGetColorTableParameterfv = 330;
        public static final int glGetColorTableParameteriv = 331;
        public static final int glColorSubTable = 332;
        public static final int glCopyColorSubTable = 333;
        public static final int glConvolutionFilter1D = 334;
        public static final int glConvolutionFilter2D = 335;
        public static final int glConvolutionParameterf = 336;
        public static final int glConvolutionParameterfv = 337;
        public static final int glConvolutionParameteri = 338;
        public static final int glConvolutionParameteriv = 339;
        public static final int glCopyConvolutionFilter1D = 340;
        public static final int glCopyConvolutionFilter2D = 341;
        public static final int glGetConvolutionFilter = 342;
        public static final int glGetConvolutionParameterfv = 343;
        public static final int glGetConvolutionParameteriv = 344;
        public static final int glGetSeparableFilter = 345;
        public static final int glSeparableFilter2D = 346;
        public static final int glGetHistogram = 347;
        public static final int glGetHistogramParameterfv = 348;
        public static final int glGetHistogramParameteriv = 349;
        public static final int glGetMinmax = 350;
        public static final int glGetMinmaxParameterfv = 351;
        public static final int glGetMinmaxParameteriv = 352;
        public static final int glHistogram = 353;
        public static final int glMinmax = 354;
        public static final int glResetHistogram = 355;
        public static final int glResetMinmax = 356;
        public static final int glBlendBarrier = 357;
        public static final int glClipPlanef = 358;
        public static final int glGetClipPlanef = 359;
        public static final int glBufferParameteri = 360;
        public static final int glEnableIndexed = 361;
        public static final int glDisableIndexed = 362;
        public static final int glIsEnabledIndexed = 363;
        public static final int glGetIntegerIndexedv = 364;
        public static final int glGetBooleanIndexedv = 365;
        public static final int glColorMaskIndexed = 366;
        public static final int glClearColorIi = 367;
        public static final int glClearColorIui = 368;
        public static final int glVertex2h = 369;
        public static final int glVertex2hv = 370;
        public static final int glVertex3h = 371;
        public static final int glVertex3hv = 372;
        public static final int glVertex4h = 373;
        public static final int glVertex4hv = 374;
        public static final int glNormal3h = 375;
        public static final int glNormal3hv = 376;
        public static final int glColor3h = 377;
        public static final int glColor3hv = 378;
        public static final int glColor4h = 379;
        public static final int glColor4hv = 380;
        public static final int glTexCoord1h = 381;
        public static final int glTexCoord1hv = 382;
        public static final int glTexCoord2h = 383;
        public static final int glTexCoord2hv = 384;
        public static final int glTexCoord3h = 385;
        public static final int glTexCoord3hv = 386;
        public static final int glTexCoord4h = 387;
        public static final int glTexCoord4hv = 388;
        public static final int glMultiTexCoord1h = 389;
        public static final int glMultiTexCoord1hv = 390;
        public static final int glMultiTexCoord2h = 391;
        public static final int glMultiTexCoord2hv = 392;
        public static final int glMultiTexCoord3h = 393;
        public static final int glMultiTexCoord3hv = 394;
        public static final int glMultiTexCoord4h = 395;
        public static final int glMultiTexCoord4hv = 396;
        public static final int glFogCoordh = 397;
        public static final int glFogCoordhv = 398;
        public static final int glSecondaryColor3h = 399;
        public static final int glSecondaryColor3hv = 400;
        public static final int glVertexWeighth = 401;
        public static final int glVertexWeighthv = 402;
        public static final int glVertexAttrib1h = 403;
        public static final int glVertexAttrib1hv = 404;
        public static final int glVertexAttrib2h = 405;
        public static final int glVertexAttrib2hv = 406;
        public static final int glVertexAttrib3h = 407;
        public static final int glVertexAttrib3hv = 408;
        public static final int glVertexAttrib4h = 409;
        public static final int glVertexAttrib4hv = 410;
        public static final int glVertexAttribs1hv = 411;
        public static final int glVertexAttribs2hv = 412;
        public static final int glVertexAttribs3hv = 413;
        public static final int glVertexAttribs4hv = 414;
        public static final int glVertexAttribPointer = 415;
        public static final int glDrawElementsInstanced = 416;
        public static final int glDrawRangeElements = 417;
        public static final int glVertexAttribIPointer = 418;
        public static final int glAlphaFunc = 419;
        public static final int glFogf = 420;
        public static final int glFogfv = 421;
        public static final int glGetLightfv = 422;
        public static final int glGetMaterialfv = 423;
        public static final int glGetTexEnvfv = 424;
        public static final int glLightModelf = 425;
        public static final int glLightModelfv = 426;
        public static final int glLightf = 427;
        public static final int glMultiTexCoord4f = 428;
        public static final int glNormal3f = 429;
        public static final int glPointParameterf = 430;
        public static final int glPointParameterfv = 431;
        public static final int glPointSize = 432;
        public static final int glTexEnvf = 433;
        public static final int glTexEnvfv = 434;
        public static final int glClientActiveTexture = 435;
        public static final int glColor4ub = 436;
        public static final int glGetTexEnviv = 437;
        public static final int glLogicOp = 438;
        public static final int glTexEnvi = 439;
        public static final int glTexEnviv = 440;
        public static final int glOrtho = 441;
        public static final int glFrustum = 442;
        public static final int glDrawElements = 443;
        public static final int glActiveTexture = 444;
        public static final int glBindBuffer = 445;
        public static final int glBindFramebuffer = 446;
        public static final int glBindRenderbuffer = 447;
        public static final int glBindTexture = 448;
        public static final int glBlendEquation = 449;
        public static final int glBlendEquationSeparate = 450;
        public static final int glBlendFunc = 451;
        public static final int glBlendFuncSeparate = 452;
        public static final int glBufferData = 453;
        public static final int glBufferSubData = 454;
        public static final int glCheckFramebufferStatus = 455;
        public static final int glClear = 456;
        public static final int glClearColor = 457;
        public static final int glClearDepthf = 458;
        public static final int glClearStencil = 459;
        public static final int glColorMask = 460;
        public static final int glCompressedTexImage2D = 461;
        public static final int glCompressedTexSubImage2D = 462;
        public static final int glCopyTexImage2D = 463;
        public static final int glCopyTexSubImage2D = 464;
        public static final int glCullFace = 465;
        public static final int glDeleteBuffers = 466;
        public static final int glDeleteFramebuffers = 467;
        public static final int glDeleteRenderbuffers = 468;
        public static final int glDeleteTextures = 469;
        public static final int glDepthFunc = 470;
        public static final int glDepthMask = 471;
        public static final int glDepthRangef = 472;
        public static final int glDisable = 473;
        public static final int glDrawArrays = 474;
        public static final int glEnable = 475;
        public static final int glFinish = 476;
        public static final int glFlush = 477;
        public static final int glFramebufferRenderbuffer = 478;
        public static final int glFramebufferTexture2D = 479;
        public static final int glFrontFace = 480;
        public static final int glGenBuffers = 481;
        public static final int glGenerateMipmap = 482;
        public static final int glGenFramebuffers = 483;
        public static final int glGenRenderbuffers = 484;
        public static final int glGenTextures = 485;
        public static final int glGetBooleanv = 486;
        public static final int glGetBufferParameteriv = 487;
        public static final int glGetError = 488;
        public static final int glGetFloatv = 489;
        public static final int glGetFramebufferAttachmentParameteriv = 490;
        public static final int glGetIntegerv = 491;
        public static final int glGetRenderbufferParameteriv = 492;
        public static final int glGetString = 493;
        public static final int glGetTexParameterfv = 494;
        public static final int glGetTexParameteriv = 495;
        public static final int glHint = 496;
        public static final int glIsBuffer = 497;
        public static final int glIsEnabled = 498;
        public static final int glIsFramebuffer = 499;
        public static final int glIsRenderbuffer = 500;
        public static final int glIsTexture = 501;
        public static final int glLineWidth = 502;
        public static final int glPixelStorei = 503;
        public static final int glPolygonOffset = 504;
        public static final int glReadPixels = 505;
        public static final int glRenderbufferStorage = 506;
        public static final int glSampleCoverage = 507;
        public static final int glScissor = 508;
        public static final int glStencilFunc = 509;
        public static final int glStencilMask = 510;
        public static final int glStencilOp = 511;
        public static final int glTexImage2D = 512;
        public static final int glTexParameterf = 513;
        public static final int glTexParameterfv = 514;
        public static final int glTexParameteri = 515;
        public static final int glTexParameteriv = 516;
        public static final int glTexSubImage2D = 517;
        public static final int glViewport = 518;
        public static final int glTexStorage1D = 519;
        public static final int glTexStorage2D = 520;
        public static final int glTexStorage3D = 521;
        public static final int glUnmapBuffer = 522;
        public static final int glRenderbufferStorageMultisample = 523;
        public static final int glFlushMappedBufferRange = 524;
        public static final int glGetGraphicsResetStatus = 525;
        public static final int glReadnPixels = 526;
        public static final int glGetnUniformfv = 527;
        public static final int glGetnUniformiv = 528;
        public static final int glClearDepth = 529;
        public static final int glDepthRange = 530;
        public static final int glMatrixMode = 531;
        public static final int glPushMatrix = 532;
        public static final int glPopMatrix = 533;
        public static final int glLoadIdentity = 534;
        public static final int glLoadMatrixf = 535;
        public static final int glMultMatrixf = 536;
        public static final int glTranslatef = 537;
        public static final int glRotatef = 538;
        public static final int glScalef = 539;
        public static final int glOrthof = 540;
        public static final int glFrustumf = 541;
        public static final int glEnableClientState = 542;
        public static final int glDisableClientState = 543;
        public static final int glVertexPointer = 544;
        public static final int glColorPointer = 545;
        public static final int glColor4f = 546;
        public static final int glNormalPointer = 547;
        public static final int glTexCoordPointer = 548;
        public static final int glLightfv = 549;
        public static final int glMaterialf = 550;
        public static final int glMaterialfv = 551;
        public static final int glShadeModel = 552;
        public static final int glPolygonMode = 553;
        public static final int glDrawBuffer = 554;
        public static final int glGetDoublev = 555;
        public static final int glPixelStoref = 556;
        public static final int glTexImage1D = 557;
        public static final int glGetTexImage = 558;
        public static final int glTexSubImage1D = 559;
        public static final int glCopyTexImage1D = 560;
        public static final int glCopyTexSubImage1D = 561;
        public static final int glCompressedTexImage1D = 562;
        public static final int glCompressedTexSubImage1D = 563;
        public static final int glGetCompressedTexImage = 564;
        public static final int glMultiDrawArrays = 565;
        public static final int glMultiDrawElements = 566;
        public static final int glPointParameteri = 567;
        public static final int glPointParameteriv = 568;
        public static final int glGetBufferSubData = 569;
        public static final int glGetVertexAttribdv = 570;
        public static final int glVertexAttrib1d = 571;
        public static final int glVertexAttrib1dv = 572;
        public static final int glVertexAttrib1s = 573;
        public static final int glVertexAttrib1sv = 574;
        public static final int glVertexAttrib2d = 575;
        public static final int glVertexAttrib2dv = 576;
        public static final int glVertexAttrib2s = 577;
        public static final int glVertexAttrib2sv = 578;
        public static final int glVertexAttrib3d = 579;
        public static final int glVertexAttrib3dv = 580;
        public static final int glVertexAttrib3s = 581;
        public static final int glVertexAttrib3sv = 582;
        public static final int glVertexAttrib4Nbv = 583;
        public static final int glVertexAttrib4Niv = 584;
        public static final int glVertexAttrib4Nsv = 585;
        public static final int glVertexAttrib4Nub = 586;
        public static final int glVertexAttrib4Nubv = 587;
        public static final int glVertexAttrib4Nuiv = 588;
        public static final int glVertexAttrib4Nusv = 589;
        public static final int glVertexAttrib4bv = 590;
        public static final int glVertexAttrib4d = 591;
        public static final int glVertexAttrib4dv = 592;
        public static final int glVertexAttrib4iv = 593;
        public static final int glVertexAttrib4s = 594;
        public static final int glVertexAttrib4sv = 595;
        public static final int glVertexAttrib4ubv = 596;
        public static final int glVertexAttrib4uiv = 597;
        public static final int glVertexAttrib4usv = 598;
        public static final int glClampColor = 599;
        public static final int glVertexAttribI1i = 600;
        public static final int glVertexAttribI2i = 601;
        public static final int glVertexAttribI3i = 602;
        public static final int glVertexAttribI1ui = 603;
        public static final int glVertexAttribI2ui = 604;
        public static final int glVertexAttribI3ui = 605;
        public static final int glVertexAttribI1iv = 606;
        public static final int glVertexAttribI2iv = 607;
        public static final int glVertexAttribI3iv = 608;
        public static final int glVertexAttribI1uiv = 609;
        public static final int glVertexAttribI2uiv = 610;
        public static final int glVertexAttribI3uiv = 611;
        public static final int glVertexAttribI4bv = 612;
        public static final int glVertexAttribI4sv = 613;
        public static final int glVertexAttribI4ubv = 614;
        public static final int glVertexAttribI4usv = 615;
        public static final int glBindFragDataLocation = 616;
        public static final int glFramebufferTexture1D = 617;
        public static final int glPrimitiveRestartIndex = 618;
        public static final int glGetActiveUniformName = 619;
        public static final int glProvokingVertex = 620;
        public static final int glDrawTransformFeedback = 621;
        public static final int glDrawTransformFeedbackStream = 622;
        public static final int glBeginQueryIndexed = 623;
        public static final int glEndQueryIndexed = 624;
        public static final int glGetQueryIndexediv = 625;
        public static final int glProgramUniform1d = 626;
        public static final int glProgramUniform1dv = 627;
        public static final int glProgramUniform2d = 628;
        public static final int glProgramUniform2dv = 629;
        public static final int glProgramUniform3d = 630;
        public static final int glProgramUniform3dv = 631;
        public static final int glProgramUniform4d = 632;
        public static final int glProgramUniform4dv = 633;
        public static final int glProgramUniformMatrix2dv = 634;
        public static final int glProgramUniformMatrix3dv = 635;
        public static final int glProgramUniformMatrix4dv = 636;
        public static final int glProgramUniformMatrix2x3dv = 637;
        public static final int glProgramUniformMatrix3x2dv = 638;
        public static final int glProgramUniformMatrix2x4dv = 639;
        public static final int glProgramUniformMatrix4x2dv = 640;
        public static final int glProgramUniformMatrix3x4dv = 641;
        public static final int glProgramUniformMatrix4x3dv = 642;
        public static final int glVertexAttribL1d = 643;
        public static final int glVertexAttribL2d = 644;
        public static final int glVertexAttribL3d = 645;
        public static final int glVertexAttribL4d = 646;
        public static final int glVertexAttribL1dv = 647;
        public static final int glVertexAttribL2dv = 648;
        public static final int glVertexAttribL3dv = 649;
        public static final int glVertexAttribL4dv = 650;
        public static final int glVertexAttribLPointer = 651;
        public static final int glGetVertexAttribLdv = 652;
        public static final int glGetActiveAtomicCounterBufferiv = 653;
        public static final int glClearBufferData = 654;
        public static final int glClearBufferSubData = 655;
        public static final int glGetInternalformati64v = 656;
        public static final int glInvalidateTexSubImage = 657;
        public static final int glInvalidateTexImage = 658;
        public static final int glInvalidateBufferSubData = 659;
        public static final int glInvalidateBufferData = 660;
        public static final int glGetnCompressedTexImage = 661;
        public static final int glGetnTexImage = 662;
        public static final int glGetnUniformdv = 663;
        public static final int glReadBuffer = 664;
        public static final int glGetTexLevelParameterfv = 665;
        public static final int glGetTexLevelParameteriv = 666;
        public static final int glUniformMatrix2x3fv = 667;
        public static final int glUniformMatrix3x2fv = 668;
        public static final int glUniformMatrix2x4fv = 669;
        public static final int glUniformMatrix4x2fv = 670;
        public static final int glUniformMatrix3x4fv = 671;
        public static final int glUniformMatrix4x3fv = 672;
        public static final int glColorMaski = 673;
        public static final int glGetBooleani_v = 674;
        public static final int glGetIntegeri_v = 675;
        public static final int glEnablei = 676;
        public static final int glDisablei = 677;
        public static final int glIsEnabledi = 678;
        public static final int glBeginTransformFeedback = 679;
        public static final int glEndTransformFeedback = 680;
        public static final int glBindBufferRange = 681;
        public static final int glBindBufferBase = 682;
        public static final int glTransformFeedbackVaryings = 683;
        public static final int glGetTransformFeedbackVarying = 684;
        public static final int glBeginConditionalRender = 685;
        public static final int glEndConditionalRender = 686;
        public static final int glGetVertexAttribIiv = 687;
        public static final int glGetVertexAttribIuiv = 688;
        public static final int glVertexAttribI4i = 689;
        public static final int glVertexAttribI4ui = 690;
        public static final int glVertexAttribI4iv = 691;
        public static final int glVertexAttribI4uiv = 692;
        public static final int glGetUniformuiv = 693;
        public static final int glGetFragDataLocation = 694;
        public static final int glUniform1ui = 695;
        public static final int glUniform2ui = 696;
        public static final int glUniform3ui = 697;
        public static final int glUniform4ui = 698;
        public static final int glUniform1uiv = 699;
        public static final int glUniform2uiv = 700;
        public static final int glUniform3uiv = 701;
        public static final int glUniform4uiv = 702;
        public static final int glClearBufferiv = 703;
        public static final int glClearBufferuiv = 704;
        public static final int glClearBufferfv = 705;
        public static final int glClearBufferfi = 706;
        public static final int glGetStringi = 707;
        public static final int glBlitFramebuffer = 708;
        public static final int glFramebufferTextureLayer = 709;
        public static final int glBindVertexArray = 710;
        public static final int glDeleteVertexArrays = 711;
        public static final int glGenVertexArrays = 712;
        public static final int glIsVertexArray = 713;
        public static final int glDrawArraysInstanced = 714;
        public static final int glTexBuffer = 715;
        public static final int glCopyBufferSubData = 716;
        public static final int glGetUniformIndices = 717;
        public static final int glGetActiveUniformsiv = 718;
        public static final int glGetUniformBlockIndex = 719;
        public static final int glGetActiveUniformBlockiv = 720;
        public static final int glGetActiveUniformBlockName = 721;
        public static final int glUniformBlockBinding = 722;
        public static final int glVertexAttribDivisor = 723;
        public static final int glMinSampleShading = 724;
        public static final int glBlendEquationi = 725;
        public static final int glBlendEquationSeparatei = 726;
        public static final int glBlendFunci = 727;
        public static final int glBlendFuncSeparatei = 728;
        public static final int glBindTransformFeedback = 729;
        public static final int glDeleteTransformFeedbacks = 730;
        public static final int glGenTransformFeedbacks = 731;
        public static final int glIsTransformFeedback = 732;
        public static final int glPauseTransformFeedback = 733;
        public static final int glResumeTransformFeedback = 734;
        public static final int glGetInternalformativ = 735;
        public static final int glBindImageTexture = 736;
        public static final int glMemoryBarrier = 737;
        public static final int glFramebufferParameteri = 738;
        public static final int glGetFramebufferParameteriv = 739;
        public static final int glInvalidateFramebuffer = 740;
        public static final int glInvalidateSubFramebuffer = 741;
        public static final int glTexStorage2DMultisample = 742;
        public static final int glTexStorage3DMultisample = 743;
        public static final int glGetnUniformuiv = 744;
        public static final int glPrimitiveBoundingBox = 745;
        public static final int glAttachShader = 746;
        public static final int glBindAttribLocation = 747;
        public static final int glBlendColor = 748;
        public static final int glCompileShader = 749;
        public static final int glCreateProgram = 750;
        public static final int glCreateShader = 751;
        public static final int glDeleteProgram = 752;
        public static final int glDeleteShader = 753;
        public static final int glDetachShader = 754;
        public static final int glDisableVertexAttribArray = 755;
        public static final int glEnableVertexAttribArray = 756;
        public static final int glGetActiveAttrib = 757;
        public static final int glGetActiveUniform = 758;
        public static final int glGetAttachedShaders = 759;
        public static final int glGetAttribLocation = 760;
        public static final int glGetProgramiv = 761;
        public static final int glGetProgramInfoLog = 762;
        public static final int glGetShaderiv = 763;
        public static final int glGetShaderInfoLog = 764;
        public static final int glGetShaderSource = 765;
        public static final int glGetUniformfv = 766;
        public static final int glGetUniformiv = 767;
        public static final int glGetUniformLocation = 768;
        public static final int glGetVertexAttribfv = 769;
        public static final int glGetVertexAttribiv = 770;
        public static final int glIsProgram = 771;
        public static final int glIsShader = 772;
        public static final int glLinkProgram = 773;
        public static final int glShaderSource = 774;
        public static final int glStencilFuncSeparate = 775;
        public static final int glStencilMaskSeparate = 776;
        public static final int glStencilOpSeparate = 777;
        public static final int glUniform1f = 778;
        public static final int glUniform1fv = 779;
        public static final int glUniform1i = 780;
        public static final int glUniform1iv = 781;
        public static final int glUniform2f = 782;
        public static final int glUniform2fv = 783;
        public static final int glUniform2i = 784;
        public static final int glUniform2iv = 785;
        public static final int glUniform3f = 786;
        public static final int glUniform3fv = 787;
        public static final int glUniform3i = 788;
        public static final int glUniform3iv = 789;
        public static final int glUniform4f = 790;
        public static final int glUniform4fv = 791;
        public static final int glUniform4i = 792;
        public static final int glUniform4iv = 793;
        public static final int glUniformMatrix2fv = 794;
        public static final int glUniformMatrix3fv = 795;
        public static final int glUniformMatrix4fv = 796;
        public static final int glUseProgram = 797;
        public static final int glValidateProgram = 798;
        public static final int glVertexAttrib1f = 799;
        public static final int glVertexAttrib1fv = 800;
        public static final int glVertexAttrib2f = 801;
        public static final int glVertexAttrib2fv = 802;
        public static final int glVertexAttrib3f = 803;
        public static final int glVertexAttrib3fv = 804;
        public static final int glVertexAttrib4f = 805;
        public static final int glVertexAttrib4fv = 806;
        public static final int glTexImage2DMultisample = 807;
        public static final int glTexImage3DMultisample = 808;
        public static final int glGetMultisamplefv = 809;
        public static final int glSampleMaski = 810;
        public static final int glDebugMessageControl = 811;
        public static final int glDebugMessageInsert = 812;
        public static final int glGetDebugMessageLog = 813;
        public static final int glPushDebugGroup = 814;
        public static final int glPopDebugGroup = 815;
        public static final int glObjectLabel = 816;
        public static final int glGetObjectLabel = 817;
        public static final int glObjectPtrLabel = 818;
        public static final int glGetObjectPtrLabel = 819;
        public static final int glCopyImageSubData = 820;
        public static final int glGetProgramBinary = 821;
        public static final int glProgramBinary = 822;
        public static final int glTexImage3D = 823;
        public static final int glTexSubImage3D = 824;
        public static final int glCopyTexSubImage3D = 825;
        public static final int glCompressedTexImage3D = 826;
        public static final int glCompressedTexSubImage3D = 827;
        public static final int glFramebufferTexture3D = 828;
        public static final int glTexParameterIiv = 829;
        public static final int glTexParameterIuiv = 830;
        public static final int glGetTexParameterIiv = 831;
        public static final int glGetTexParameterIuiv = 832;
        public static final int glSamplerParameterIiv = 833;
        public static final int glSamplerParameterIuiv = 834;
        public static final int glGetSamplerParameterIiv = 835;
        public static final int glGetSamplerParameterIuiv = 836;
        public static final int glDrawArraysInstancedBaseInstance = 837;
        public static final int glDrawElementsInstancedBaseInstance = 838;
        public static final int glDrawElementsInstancedBaseVertexBaseInstance = 839;
        public static final int glGenQueries = 840;
        public static final int glDeleteQueries = 841;
        public static final int glIsQuery = 842;
        public static final int glBeginQuery = 843;
        public static final int glEndQuery = 844;
        public static final int glQueryCounter = 845;
        public static final int glGetQueryiv = 846;
        public static final int glGetQueryObjectiv = 847;
        public static final int glGetQueryObjectuiv = 848;
        public static final int glGetQueryObjecti64v = 849;
        public static final int glGetQueryObjectui64v = 850;
        public static final int glActiveShaderProgram = 851;
        public static final int glBindProgramPipeline = 852;
        public static final int glCreateShaderProgramv = 853;
        public static final int glDeleteProgramPipelines = 854;
        public static final int glGenProgramPipelines = 855;
        public static final int glGetProgramPipelineInfoLog = 856;
        public static final int glGetProgramPipelineiv = 857;
        public static final int glIsProgramPipeline = 858;
        public static final int glProgramParameteri = 859;
        public static final int glProgramUniform1f = 860;
        public static final int glProgramUniform1fv = 861;
        public static final int glProgramUniform1i = 862;
        public static final int glProgramUniform1iv = 863;
        public static final int glProgramUniform2f = 864;
        public static final int glProgramUniform2fv = 865;
        public static final int glProgramUniform2i = 866;
        public static final int glProgramUniform2iv = 867;
        public static final int glProgramUniform3f = 868;
        public static final int glProgramUniform3fv = 869;
        public static final int glProgramUniform3i = 870;
        public static final int glProgramUniform3iv = 871;
        public static final int glProgramUniform4f = 872;
        public static final int glProgramUniform4fv = 873;
        public static final int glProgramUniform4i = 874;
        public static final int glProgramUniform4iv = 875;
        public static final int glProgramUniformMatrix2fv = 876;
        public static final int glProgramUniformMatrix3fv = 877;
        public static final int glProgramUniformMatrix4fv = 878;
        public static final int glUseProgramStages = 879;
        public static final int glValidateProgramPipeline = 880;
        public static final int glProgramUniform1ui = 881;
        public static final int glProgramUniform2ui = 882;
        public static final int glProgramUniform3ui = 883;
        public static final int glProgramUniform4ui = 884;
        public static final int glProgramUniform1uiv = 885;
        public static final int glProgramUniform2uiv = 886;
        public static final int glProgramUniform3uiv = 887;
        public static final int glProgramUniform4uiv = 888;
        public static final int glProgramUniformMatrix2x3fv = 889;
        public static final int glProgramUniformMatrix3x2fv = 890;
        public static final int glProgramUniformMatrix2x4fv = 891;
        public static final int glProgramUniformMatrix4x2fv = 892;
        public static final int glProgramUniformMatrix3x4fv = 893;
        public static final int glProgramUniformMatrix4x3fv = 894;
        public static final int glDrawBuffers = 895;
        public static final int glReleaseShaderCompiler = 896;
        public static final int glShaderBinary = 897;
        public static final int glGetShaderPrecisionFormat = 898;
        public static final int glUniform = 899;
    }

    private final int[] methodCallsZeroValues = new int[900];
    private final int[] methodCalls = new int[900];
    private GL2 gl;
    private Thread thread;
    private boolean drawingDisabled;

    private void verifyAccess() {
        if (thread != null && Thread.currentThread() != thread) {
            throw new IllegalStateException(
                "Illegal cross-thread OpenGL access: expected = " + thread + "; currentThread = " + Thread.currentThread());
        }
    }

    public void setGL2(GL2 gl) {
        this.gl = gl;
    }

    public boolean isInitialized() {
        return gl != null;
    }

    public void setDrawingDisabled(boolean disabled) {
        this.drawingDisabled = disabled;
    }

    public boolean isDrawingDisabled() {
        return drawingDisabled;
    }

    public void setVerifyThread(Thread thread) {
        this.thread = thread;
    }

    public void resetCounters() {
        System.arraycopy(methodCallsZeroValues, 0, methodCalls, 0, methodCalls.length);
    }

    public int getCounter(int method) {
        return methodCalls[method];
    }

    @Override
    public void glClearIndex(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearIndex];
        gl.glClearIndex(v);
    }

    @Override
    public void glIndexMask(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexMask];
        gl.glIndexMask(i);
    }

    @Override
    public void glLineStipple(int i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glLineStipple];
        gl.glLineStipple(i, i1);
    }

    @Override
    public void glPolygonStipple(ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPolygonStipple];
        gl.glPolygonStipple(byteBuffer);
    }

    @Override
    public void glPolygonStipple(byte[] bytes, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glPolygonStipple];
        gl.glPolygonStipple(bytes, i);
    }

    @Override
    public void glPolygonStipple(long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glPolygonStipple];
        gl.glPolygonStipple(l);
    }

    @Override
    public void glGetPolygonStipple(ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPolygonStipple];
        gl.glGetPolygonStipple(byteBuffer);
    }

    @Override
    public void glGetPolygonStipple(byte[] bytes, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPolygonStipple];
        gl.glGetPolygonStipple(bytes, i);
    }

    @Override
    public void glGetPolygonStipple(long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPolygonStipple];
        gl.glGetPolygonStipple(l);
    }

    @Override
    public void glEdgeFlag(boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glEdgeFlag];
        gl.glEdgeFlag(b);
    }

    @Override
    public void glEdgeFlagv(ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEdgeFlagv];
        gl.glEdgeFlagv(byteBuffer);
    }

    @Override
    public void glEdgeFlagv(byte[] bytes, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEdgeFlagv];
        gl.glEdgeFlagv(bytes, i);
    }

    @Override
    public void glClipPlane(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClipPlane];
        gl.glClipPlane(i, doubleBuffer);
    }

    @Override
    public void glClipPlane(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glClipPlane];
        gl.glClipPlane(i, doubles, i1);
    }

    @Override
    public void glGetClipPlane(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetClipPlane];
        gl.glGetClipPlane(i, doubleBuffer);
    }

    @Override
    public void glGetClipPlane(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetClipPlane];
        gl.glGetClipPlane(i, doubles, i1);
    }

    @Override
    public void glPushAttrib(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glPushAttrib];
        gl.glPushAttrib(i);
    }

    @Override
    public void glPopAttrib() {
        verifyAccess();
        ++methodCalls[GLFunc.glPopAttrib];
        gl.glPopAttrib();
    }

    @Override
    public int glRenderMode(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRenderMode];
        return gl.glRenderMode(i);
    }

    @Override
    public void glClearAccum(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearAccum];
        gl.glClearAccum(v, v1, v2, v3);
    }

    @Override
    public void glAccum(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glAccum];
        gl.glAccum(i, v);
    }

    @Override
    public void glLoadMatrixd(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadMatrixd];
        gl.glLoadMatrixd(doubleBuffer);
    }

    @Override
    public void glLoadMatrixd(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadMatrixd];
        gl.glLoadMatrixd(doubles, i);
    }

    @Override
    public void glMultMatrixd(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultMatrixd];
        gl.glMultMatrixd(doubleBuffer);
    }

    @Override
    public void glMultMatrixd(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultMatrixd];
        gl.glMultMatrixd(doubles, i);
    }

    @Override
    public void glRotated(double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRotated];
        gl.glRotated(v, v1, v2, v3);
    }

    @Override
    public void glScaled(double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glScaled];
        gl.glScaled(v, v1, v2);
    }

    @Override
    public void glTranslated(double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTranslated];
        gl.glTranslated(v, v1, v2);
    }

    @Override
    public boolean glIsList(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsList];
        return gl.glIsList(i);
    }

    @Override
    public void glDeleteLists(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteLists];
        gl.glDeleteLists(i, i1);
    }

    @Override
    public int glGenLists(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenLists];
        return gl.glGenLists(i);
    }

    @Override
    public void glNewList(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glNewList];
        gl.glNewList(i, i1);
    }

    @Override
    public void glEndList() {
        verifyAccess();
        ++methodCalls[GLFunc.glEndList];
        gl.glEndList();
    }

    @Override
    public void glCallList(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glCallList];
        gl.glCallList(i);
    }

    @Override
    public void glCallLists(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCallLists];
        gl.glCallLists(i, i1, buffer);
    }

    @Override
    public void glListBase(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glListBase];
        gl.glListBase(i);
    }

    @Override
    public void glBegin(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glBegin];
        gl.glBegin(i);
    }

    @Override
    public void glEnd() {
        verifyAccess();
        ++methodCalls[GLFunc.glEnd];
        gl.glEnd();
    }

    @Override
    public void glVertex2d(double v, double v1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2d];
        gl.glVertex2d(v, v1);
    }

    @Override
    public void glVertex2f(float v, float v1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2f];
        gl.glVertex2f(v, v1);
    }

    @Override
    public void glVertex2i(int i, int i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2i];
        gl.glVertex2i(i, i1);
    }

    @Override
    public void glVertex2s(short i, short i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2s];
        gl.glVertex2s(i, i1);
    }

    @Override
    public void glVertex3d(double v, double v1, double v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3d];
        gl.glVertex3d(v, v1, v2);
    }

    @Override
    public void glVertex3f(float v, float v1, float v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3f];
        gl.glVertex3f(v, v1, v2);
    }

    @Override
    public void glVertex3i(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3i];
        gl.glVertex3i(i, i1, i2);
    }

    @Override
    public void glVertex3s(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3s];
        gl.glVertex3s(i, i1, i2);
    }

    @Override
    public void glVertex4d(double v, double v1, double v2, double v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4d];
        gl.glVertex4d(v, v1, v2, v3);
    }

    @Override
    public void glVertex4f(float v, float v1, float v2, float v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4f];
        gl.glVertex4f(v, v1, v2, v3);
    }

    @Override
    public void glVertex4i(int i, int i1, int i2, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4i];
        gl.glVertex4i(i, i1, i2, i3);
    }

    @Override
    public void glVertex4s(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4s];
        gl.glVertex4s(i, i1, i2, i3);
    }

    @Override
    public void glVertex2dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2dv];
        gl.glVertex2dv(doubleBuffer);
    }

    @Override
    public void glVertex2dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2dv];
        gl.glVertex2dv(doubles, i);
    }

    @Override
    public void glVertex2fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2fv];
        gl.glVertex2fv(floatBuffer);
    }

    @Override
    public void glVertex2fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2fv];
        gl.glVertex2fv(floats, i);
    }

    @Override
    public void glVertex2iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2iv];
        gl.glVertex2iv(intBuffer);
    }

    @Override
    public void glVertex2iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2iv];
        gl.glVertex2iv(ints, i);
    }

    @Override
    public void glVertex2sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2sv];
        gl.glVertex2sv(shortBuffer);
    }

    @Override
    public void glVertex2sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2sv];
        gl.glVertex2sv(shorts, i);
    }

    @Override
    public void glVertex3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3dv];
        gl.glVertex3dv(doubleBuffer);
    }

    @Override
    public void glVertex3dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3dv];
        gl.glVertex3dv(doubles, i);
    }

    @Override
    public void glVertex3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3fv];
        gl.glVertex3fv(floatBuffer);
    }

    @Override
    public void glVertex3fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3fv];
        gl.glVertex3fv(floats, i);
    }

    @Override
    public void glVertex3iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3iv];
        gl.glVertex3iv(intBuffer);
    }

    @Override
    public void glVertex3iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3iv];
        gl.glVertex3iv(ints, i);
    }

    @Override
    public void glVertex3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3sv];
        gl.glVertex3sv(shortBuffer);
    }

    @Override
    public void glVertex3sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3sv];
        gl.glVertex3sv(shorts, i);
    }

    @Override
    public void glVertex4dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4dv];
        gl.glVertex4dv(doubleBuffer);
    }

    @Override
    public void glVertex4dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4dv];
        gl.glVertex4dv(doubles, i);
    }

    @Override
    public void glVertex4fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4fv];
        gl.glVertex4fv(floatBuffer);
    }

    @Override
    public void glVertex4fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4fv];
        gl.glVertex4fv(floats, i);
    }

    @Override
    public void glVertex4iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4iv];
        gl.glVertex4iv(intBuffer);
    }

    @Override
    public void glVertex4iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4iv];
        gl.glVertex4iv(ints, i);
    }

    @Override
    public void glVertex4sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4sv];
        gl.glVertex4sv(shortBuffer);
    }

    @Override
    public void glVertex4sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4sv];
        gl.glVertex4sv(shorts, i);
    }

    @Override
    public void glNormal3b(byte b, byte b1, byte b2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3b];
        gl.glNormal3b(b, b1, b2);
    }

    @Override
    public void glNormal3d(double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3d];
        gl.glNormal3d(v, v1, v2);
    }

    @Override
    public void glNormal3i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3i];
        gl.glNormal3i(i, i1, i2);
    }

    @Override
    public void glNormal3s(short i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3s];
        gl.glNormal3s(i, i1, i2);
    }

    @Override
    public void glNormal3bv(ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3bv];
        gl.glNormal3bv(byteBuffer);
    }

    @Override
    public void glNormal3bv(byte[] bytes, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3bv];
        gl.glNormal3bv(bytes, i);
    }

    @Override
    public void glNormal3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3dv];
        gl.glNormal3dv(doubleBuffer);
    }

    @Override
    public void glNormal3dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3dv];
        gl.glNormal3dv(doubles, i);
    }

    @Override
    public void glNormal3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3fv];
        gl.glNormal3fv(floatBuffer);
    }

    @Override
    public void glNormal3fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3fv];
        gl.glNormal3fv(floats, i);
    }

    @Override
    public void glNormal3iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3iv];
        gl.glNormal3iv(intBuffer);
    }

    @Override
    public void glNormal3iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3iv];
        gl.glNormal3iv(ints, i);
    }

    @Override
    public void glNormal3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3sv];
        gl.glNormal3sv(shortBuffer);
    }

    @Override
    public void glNormal3sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3sv];
        gl.glNormal3sv(shorts, i);
    }

    @Override
    public void glIndexd(double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexd];
        gl.glIndexd(v);
    }

    @Override
    public void glIndexf(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexf];
        gl.glIndexf(v);
    }

    @Override
    public void glIndexi(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexi];
        gl.glIndexi(i);
    }

    @Override
    public void glIndexs(short i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexs];
        gl.glIndexs(i);
    }

    @Override
    public void glIndexdv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexdv];
        gl.glIndexdv(doubleBuffer);
    }

    @Override
    public void glIndexdv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexdv];
        gl.glIndexdv(doubles, i);
    }

    @Override
    public void glIndexfv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexfv];
        gl.glIndexfv(floatBuffer);
    }

    @Override
    public void glIndexfv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexfv];
        gl.glIndexfv(floats, i);
    }

    @Override
    public void glIndexiv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexiv];
        gl.glIndexiv(intBuffer);
    }

    @Override
    public void glIndexiv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexiv];
        gl.glIndexiv(ints, i);
    }

    @Override
    public void glIndexsv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexsv];
        gl.glIndexsv(shortBuffer);
    }

    @Override
    public void glIndexsv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexsv];
        gl.glIndexsv(shorts, i);
    }

    @Override
    public void glColor3b(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3b];
        gl.glColor3b(b, b1, b2);
    }

    @Override
    public void glColor3d(double v, double v1, double v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3d];
        gl.glColor3d(v, v1, v2);
    }

    @Override
    public void glColor3f(float v, float v1, float v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3f];
        gl.glColor3f(v, v1, v2);
    }

    @Override
    public void glColor3i(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3i];
        gl.glColor3i(i, i1, i2);
    }

    @Override
    public void glColor3s(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3s];
        gl.glColor3s(i, i1, i2);
    }

    @Override
    public void glColor3ub(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3ub];
        gl.glColor3ub(b, b1, b2);
    }

    @Override
    public void glColor3ui(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3ui];
        gl.glColor3ui(i, i1, i2);
    }

    @Override
    public void glColor3us(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3us];
        gl.glColor3us(i, i1, i2);
    }

    @Override
    public void glColor4b(byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4b];
        gl.glColor4b(b, b1, b2, b3);
    }

    @Override
    public void glColor4d(double v, double v1, double v2, double v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4d];
        gl.glColor4d(v, v1, v2, v3);
    }

    @Override
    public void glColor4i(int i, int i1, int i2, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4i];
        gl.glColor4i(i, i1, i2, i3);
    }

    @Override
    public void glColor4s(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4s];
        gl.glColor4s(i, i1, i2, i3);
    }

    @Override
    public void glColor4ui(int i, int i1, int i2, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4ui];
        gl.glColor4ui(i, i1, i2, i3);
    }

    @Override
    public void glColor4us(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4us];
        gl.glColor4us(i, i1, i2, i3);
    }

    @Override
    public void glColor3bv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3bv];
        gl.glColor3bv(byteBuffer);
    }

    @Override
    public void glColor3bv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3bv];
        gl.glColor3bv(bytes, i);
    }

    @Override
    public void glColor3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3dv];
        gl.glColor3dv(doubleBuffer);
    }

    @Override
    public void glColor3dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3dv];
        gl.glColor3dv(doubles, i);
    }

    @Override
    public void glColor3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3fv];
        gl.glColor3fv(floatBuffer);
    }

    @Override
    public void glColor3fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3fv];
        gl.glColor3fv(floats, i);
    }

    @Override
    public void glColor3iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3iv];
        gl.glColor3iv(intBuffer);
    }

    @Override
    public void glColor3iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3iv];
        gl.glColor3iv(ints, i);
    }

    @Override
    public void glColor3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3sv];
        gl.glColor3sv(shortBuffer);
    }

    @Override
    public void glColor3sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3sv];
        gl.glColor3sv(shorts, i);
    }

    @Override
    public void glColor3ubv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3ubv];
        gl.glColor3ubv(byteBuffer);
    }

    @Override
    public void glColor3ubv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3ubv];
        gl.glColor3ubv(bytes, i);
    }

    @Override
    public void glColor3uiv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3uiv];
        gl.glColor3uiv(intBuffer);
    }

    @Override
    public void glColor3uiv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3uiv];
        gl.glColor3uiv(ints, i);
    }

    @Override
    public void glColor3usv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3usv];
        gl.glColor3usv(shortBuffer);
    }

    @Override
    public void glColor3usv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3usv];
        gl.glColor3usv(shorts, i);
    }

    @Override
    public void glColor4bv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4bv];
        gl.glColor4bv(byteBuffer);
    }

    @Override
    public void glColor4bv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4bv];
        gl.glColor4bv(bytes, i);
    }

    @Override
    public void glColor4dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4dv];
        gl.glColor4dv(doubleBuffer);
    }

    @Override
    public void glColor4dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4dv];
        gl.glColor4dv(doubles, i);
    }

    @Override
    public void glColor4fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4fv];
        gl.glColor4fv(floatBuffer);
    }

    @Override
    public void glColor4fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4fv];
        gl.glColor4fv(floats, i);
    }

    @Override
    public void glColor4iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4iv];
        gl.glColor4iv(intBuffer);
    }

    @Override
    public void glColor4iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4iv];
        gl.glColor4iv(ints, i);
    }

    @Override
    public void glColor4sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4sv];
        gl.glColor4sv(shortBuffer);
    }

    @Override
    public void glColor4sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4sv];
        gl.glColor4sv(shorts, i);
    }

    @Override
    public void glColor4ubv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4ubv];
        gl.glColor4ubv(byteBuffer);
    }

    @Override
    public void glColor4ubv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4ubv];
        gl.glColor4ubv(bytes, i);
    }

    @Override
    public void glColor4uiv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4uiv];
        gl.glColor4uiv(intBuffer);
    }

    @Override
    public void glColor4uiv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4uiv];
        gl.glColor4uiv(ints, i);
    }

    @Override
    public void glColor4usv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4usv];
        gl.glColor4usv(shortBuffer);
    }

    @Override
    public void glColor4usv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4usv];
        gl.glColor4usv(shorts, i);
    }

    @Override
    public void glTexCoord1d(double v) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1d];
        gl.glTexCoord1d(v);
    }

    @Override
    public void glTexCoord1f(float v) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1f];
        gl.glTexCoord1f(v);
    }

    @Override
    public void glTexCoord1i(int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1i];
        gl.glTexCoord1i(i);
    }

    @Override
    public void glTexCoord1s(short i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1s];
        gl.glTexCoord1s(i);
    }

    @Override
    public void glTexCoord2d(double v, double v1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2d];
        gl.glTexCoord2d(v, v1);
    }

    @Override
    public void glTexCoord2f(float v, float v1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2f];
        gl.glTexCoord2f(v, v1);
    }

    @Override
    public void glTexCoord2i(int i, int i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2i];
        gl.glTexCoord2i(i, i1);
    }

    @Override
    public void glTexCoord2s(short i, short i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2s];
        gl.glTexCoord2s(i, i1);
    }

    @Override
    public void glTexCoord3d(double v, double v1, double v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3d];
        gl.glTexCoord3d(v, v1, v2);
    }

    @Override
    public void glTexCoord3f(float v, float v1, float v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3f];
        gl.glTexCoord3f(v, v1, v2);
    }

    @Override
    public void glTexCoord3i(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3i];
        gl.glTexCoord3i(i, i1, i2);
    }

    @Override
    public void glTexCoord3s(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3s];
        gl.glTexCoord3s(i, i1, i2);
    }

    @Override
    public void glTexCoord4d(double v, double v1, double v2, double v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4d];
        gl.glTexCoord4d(v, v1, v2, v3);
    }

    @Override
    public void glTexCoord4f(float v, float v1, float v2, float v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4f];
        gl.glTexCoord4f(v, v1, v2, v3);
    }

    @Override
    public void glTexCoord4i(int i, int i1, int i2, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4i];
        gl.glTexCoord4i(i, i1, i2, i3);
    }

    @Override
    public void glTexCoord4s(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4s];
        gl.glTexCoord4s(i, i1, i2, i3);
    }

    @Override
    public void glTexCoord1dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1dv];
        gl.glTexCoord1dv(doubleBuffer);
    }

    @Override
    public void glTexCoord1dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1dv];
        gl.glTexCoord1dv(doubles, i);
    }

    @Override
    public void glTexCoord1fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1fv];
        gl.glTexCoord1fv(floatBuffer);
    }

    @Override
    public void glTexCoord1fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1fv];
        gl.glTexCoord1fv(floats, i);
    }

    @Override
    public void glTexCoord1iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1iv];
        gl.glTexCoord1iv(intBuffer);
    }

    @Override
    public void glTexCoord1iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1iv];
        gl.glTexCoord1iv(ints, i);
    }

    @Override
    public void glTexCoord1sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1sv];
        gl.glTexCoord1sv(shortBuffer);
    }

    @Override
    public void glTexCoord1sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1sv];
        gl.glTexCoord1sv(shorts, i);
    }

    @Override
    public void glTexCoord2dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2dv];
        gl.glTexCoord2dv(doubleBuffer);
    }

    @Override
    public void glTexCoord2dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2dv];
        gl.glTexCoord2dv(doubles, i);
    }

    @Override
    public void glTexCoord2fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2fv];
        gl.glTexCoord2fv(floatBuffer);
    }

    @Override
    public void glTexCoord2fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2fv];
        gl.glTexCoord2fv(floats, i);
    }

    @Override
    public void glTexCoord2iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2iv];
        gl.glTexCoord2iv(intBuffer);
    }

    @Override
    public void glTexCoord2iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2iv];
        gl.glTexCoord2iv(ints, i);
    }

    @Override
    public void glTexCoord2sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2sv];
        gl.glTexCoord2sv(shortBuffer);
    }

    @Override
    public void glTexCoord2sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2sv];
        gl.glTexCoord2sv(shorts, i);
    }

    @Override
    public void glTexCoord3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3dv];
        gl.glTexCoord3dv(doubleBuffer);
    }

    @Override
    public void glTexCoord3dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3dv];
        gl.glTexCoord3dv(doubles, i);
    }

    @Override
    public void glTexCoord3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3fv];
        gl.glTexCoord3fv(floatBuffer);
    }

    @Override
    public void glTexCoord3fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3fv];
        gl.glTexCoord3fv(floats, i);
    }

    @Override
    public void glTexCoord3iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3iv];
        gl.glTexCoord3iv(intBuffer);
    }

    @Override
    public void glTexCoord3iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3iv];
        gl.glTexCoord3iv(ints, i);
    }

    @Override
    public void glTexCoord3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3sv];
        gl.glTexCoord3sv(shortBuffer);
    }

    @Override
    public void glTexCoord3sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3sv];
        gl.glTexCoord3sv(shorts, i);
    }

    @Override
    public void glTexCoord4dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4dv];
        gl.glTexCoord4dv(doubleBuffer);
    }

    @Override
    public void glTexCoord4dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4dv];
        gl.glTexCoord4dv(doubles, i);
    }

    @Override
    public void glTexCoord4fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4fv];
        gl.glTexCoord4fv(floatBuffer);
    }

    @Override
    public void glTexCoord4fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4fv];
        gl.glTexCoord4fv(floats, i);
    }

    @Override
    public void glTexCoord4iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4iv];
        gl.glTexCoord4iv(intBuffer);
    }

    @Override
    public void glTexCoord4iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4iv];
        gl.glTexCoord4iv(ints, i);
    }

    @Override
    public void glTexCoord4sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4sv];
        gl.glTexCoord4sv(shortBuffer);
    }

    @Override
    public void glTexCoord4sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4sv];
        gl.glTexCoord4sv(shorts, i);
    }

    @Override
    public void glRasterPos2d(double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2d];
        gl.glRasterPos2d(v, v1);
    }

    @Override
    public void glRasterPos2f(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2f];
        gl.glRasterPos2f(v, v1);
    }

    @Override
    public void glRasterPos2i(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2i];
        gl.glRasterPos2i(i, i1);
    }

    @Override
    public void glRasterPos2s(short i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2s];
        gl.glRasterPos2s(i, i1);
    }

    @Override
    public void glRasterPos3d(double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3d];
        gl.glRasterPos3d(v, v1, v2);
    }

    @Override
    public void glRasterPos3f(float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3f];
        gl.glRasterPos3f(v, v1, v2);
    }

    @Override
    public void glRasterPos3i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3i];
        gl.glRasterPos3i(i, i1, i2);
    }

    @Override
    public void glRasterPos3s(short i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3s];
        gl.glRasterPos3s(i, i1, i2);
    }

    @Override
    public void glRasterPos4d(double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4d];
        gl.glRasterPos4d(v, v1, v2, v3);
    }

    @Override
    public void glRasterPos4f(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4f];
        gl.glRasterPos4f(v, v1, v2, v3);
    }

    @Override
    public void glRasterPos4i(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4i];
        gl.glRasterPos4i(i, i1, i2, i3);
    }

    @Override
    public void glRasterPos4s(short i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4s];
        gl.glRasterPos4s(i, i1, i2, i3);
    }

    @Override
    public void glRasterPos2dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2dv];
        gl.glRasterPos2dv(doubleBuffer);
    }

    @Override
    public void glRasterPos2dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2dv];
        gl.glRasterPos2dv(doubles, i);
    }

    @Override
    public void glRasterPos2fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2fv];
        gl.glRasterPos2fv(floatBuffer);
    }

    @Override
    public void glRasterPos2fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2fv];
        gl.glRasterPos2fv(floats, i);
    }

    @Override
    public void glRasterPos2iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2iv];
        gl.glRasterPos2iv(intBuffer);
    }

    @Override
    public void glRasterPos2iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2iv];
        gl.glRasterPos2iv(ints, i);
    }

    @Override
    public void glRasterPos2sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2sv];
        gl.glRasterPos2sv(shortBuffer);
    }

    @Override
    public void glRasterPos2sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos2sv];
        gl.glRasterPos2sv(shorts, i);
    }

    @Override
    public void glRasterPos3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3dv];
        gl.glRasterPos3dv(doubleBuffer);
    }

    @Override
    public void glRasterPos3dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3dv];
        gl.glRasterPos3dv(doubles, i);
    }

    @Override
    public void glRasterPos3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3fv];
        gl.glRasterPos3fv(floatBuffer);
    }

    @Override
    public void glRasterPos3fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3fv];
        gl.glRasterPos3fv(floats, i);
    }

    @Override
    public void glRasterPos3iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3iv];
        gl.glRasterPos3iv(intBuffer);
    }

    @Override
    public void glRasterPos3iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3iv];
        gl.glRasterPos3iv(ints, i);
    }

    @Override
    public void glRasterPos3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3sv];
        gl.glRasterPos3sv(shortBuffer);
    }

    @Override
    public void glRasterPos3sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos3sv];
        gl.glRasterPos3sv(shorts, i);
    }

    @Override
    public void glRasterPos4dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4dv];
        gl.glRasterPos4dv(doubleBuffer);
    }

    @Override
    public void glRasterPos4dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4dv];
        gl.glRasterPos4dv(doubles, i);
    }

    @Override
    public void glRasterPos4fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4fv];
        gl.glRasterPos4fv(floatBuffer);
    }

    @Override
    public void glRasterPos4fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4fv];
        gl.glRasterPos4fv(floats, i);
    }

    @Override
    public void glRasterPos4iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4iv];
        gl.glRasterPos4iv(intBuffer);
    }

    @Override
    public void glRasterPos4iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4iv];
        gl.glRasterPos4iv(ints, i);
    }

    @Override
    public void glRasterPos4sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4sv];
        gl.glRasterPos4sv(shortBuffer);
    }

    @Override
    public void glRasterPos4sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glRasterPos4sv];
        gl.glRasterPos4sv(shorts, i);
    }

    @Override
    public void glRectd(double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectd];
        gl.glRectd(v, v1, v2, v3);
    }

    @Override
    public void glRectf(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectf];
        gl.glRectf(v, v1, v2, v3);
    }

    @Override
    public void glRecti(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRecti];
        gl.glRecti(i, i1, i2, i3);
    }

    @Override
    public void glRects(short i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRects];
        gl.glRects(i, i1, i2, i3);
    }

    @Override
    public void glRectdv(DoubleBuffer doubleBuffer, DoubleBuffer doubleBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectdv];
        gl.glRectdv(doubleBuffer, doubleBuffer1);
    }

    @Override
    public void glRectdv(double[] doubles, int i, double[] doubles1, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectdv];
        gl.glRectdv(doubles, i, doubles1, i1);
    }

    @Override
    public void glRectfv(FloatBuffer floatBuffer, FloatBuffer floatBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectfv];
        gl.glRectfv(floatBuffer, floatBuffer1);
    }

    @Override
    public void glRectfv(float[] floats, int i, float[] floats1, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectfv];
        gl.glRectfv(floats, i, floats1, i1);
    }

    @Override
    public void glRectiv(IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectiv];
        gl.glRectiv(intBuffer, intBuffer1);
    }

    @Override
    public void glRectiv(int[] ints, int i, int[] ints1, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectiv];
        gl.glRectiv(ints, i, ints1, i1);
    }

    @Override
    public void glRectsv(ShortBuffer shortBuffer, ShortBuffer shortBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectsv];
        gl.glRectsv(shortBuffer, shortBuffer1);
    }

    @Override
    public void glRectsv(short[] shorts, int i, short[] shorts1, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glRectsv];
        gl.glRectsv(shorts, i, shorts1, i1);
    }

    @Override
    public void glLighti(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glLighti];
        gl.glLighti(i, i1, i2);
    }

    @Override
    public void glLightiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightiv];
        gl.glLightiv(i, i1, intBuffer);
    }

    @Override
    public void glLightiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightiv];
        gl.glLightiv(i, i1, ints, i2);
    }

    @Override
    public void glGetLightiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetLightiv];
        gl.glGetLightiv(i, i1, intBuffer);
    }

    @Override
    public void glGetLightiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetLightiv];
        gl.glGetLightiv(i, i1, ints, i2);
    }

    @Override
    public void glLightModeli(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModeli];
        gl.glLightModeli(i, i1);
    }

    @Override
    public void glLightModeliv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModeliv];
        gl.glLightModeliv(i, intBuffer);
    }

    @Override
    public void glLightModeliv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModeliv];
        gl.glLightModeliv(i, ints, i1);
    }

    @Override
    public void glMateriali(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMateriali];
        gl.glMateriali(i, i1, i2);
    }

    @Override
    public void glMaterialiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMaterialiv];
        gl.glMaterialiv(i, i1, intBuffer);
    }

    @Override
    public void glMaterialiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMaterialiv];
        gl.glMaterialiv(i, i1, ints, i2);
    }

    @Override
    public void glGetMaterialiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMaterialiv];
        gl.glGetMaterialiv(i, i1, intBuffer);
    }

    @Override
    public void glGetMaterialiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMaterialiv];
        gl.glGetMaterialiv(i, i1, ints, i2);
    }

    @Override
    public void glColorMaterial(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorMaterial];
        gl.glColorMaterial(i, i1);
    }

    @Override
    public void glPixelZoom(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelZoom];
        gl.glPixelZoom(v, v1);
    }

    @Override
    public void glPixelTransferf(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelTransferf];
        gl.glPixelTransferf(i, v);
    }

    @Override
    public void glPixelTransferi(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelTransferi];
        gl.glPixelTransferi(i, i1);
    }

    @Override
    public void glPixelMapfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapfv];
        gl.glPixelMapfv(i, i1, floatBuffer);
    }

    @Override
    public void glPixelMapfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapfv];
        gl.glPixelMapfv(i, i1, floats, i2);
    }

    @Override
    public void glPixelMapfv(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapfv];
        gl.glPixelMapfv(i, i1, l);
    }

    @Override
    public void glPixelMapuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapuiv];
        gl.glPixelMapuiv(i, i1, intBuffer);
    }

    @Override
    public void glPixelMapuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapuiv];
        gl.glPixelMapuiv(i, i1, ints, i2);
    }

    @Override
    public void glPixelMapuiv(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapuiv];
        gl.glPixelMapuiv(i, i1, l);
    }

    @Override
    public void glPixelMapusv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapusv];
        gl.glPixelMapusv(i, i1, shortBuffer);
    }

    @Override
    public void glPixelMapusv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapusv];
        gl.glPixelMapusv(i, i1, shorts, i2);
    }

    @Override
    public void glPixelMapusv(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelMapusv];
        gl.glPixelMapusv(i, i1, l);
    }

    @Override
    public void glGetPixelMapfv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapfv];
        gl.glGetPixelMapfv(i, floatBuffer);
    }

    @Override
    public void glGetPixelMapfv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapfv];
        gl.glGetPixelMapfv(i, floats, i1);
    }

    @Override
    public void glGetPixelMapfv(int i, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapfv];
        gl.glGetPixelMapfv(i, l);
    }

    @Override
    public void glGetPixelMapuiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapuiv];
        gl.glGetPixelMapuiv(i, intBuffer);
    }

    @Override
    public void glGetPixelMapuiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapuiv];
        gl.glGetPixelMapuiv(i, ints, i1);
    }

    @Override
    public void glGetPixelMapuiv(int i, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapuiv];
        gl.glGetPixelMapuiv(i, l);
    }

    @Override
    public void glGetPixelMapusv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapusv];
        gl.glGetPixelMapusv(i, shortBuffer);
    }

    @Override
    public void glGetPixelMapusv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapusv];
        gl.glGetPixelMapusv(i, shorts, i1);
    }

    @Override
    public void glGetPixelMapusv(int i, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetPixelMapusv];
        gl.glGetPixelMapusv(i, l);
    }

    @Override
    public void glBitmap(int i, int i1, float v, float v1, float v2, float v3, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glBitmap];
        gl.glBitmap(i, i1, v, v1, v2, v3, byteBuffer);
    }

    @Override
    public void glBitmap(int i, int i1, float v, float v1, float v2, float v3, byte[] bytes, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBitmap];
        gl.glBitmap(i, i1, v, v1, v2, v3, bytes, i2);
    }

    @Override
    public void glBitmap(int i, int i1, float v, float v1, float v2, float v3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glBitmap];
        gl.glBitmap(i, i1, v, v1, v2, v3, l);
    }

    @Override
    public void glDrawPixels(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawPixels];
        gl.glDrawPixels(i, i1, i2, i3, buffer);
    }

    @Override
    public void glDrawPixels(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawPixels];
        gl.glDrawPixels(i, i1, i2, i3, l);
    }

    @Override
    public void glCopyPixels(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyPixels];
        gl.glCopyPixels(i, i1, i2, i3, i4);
    }

    @Override
    public void glTexGend(int i, int i1, double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGend];
        gl.glTexGend(i, i1, v);
    }

    @Override
    public void glTexGenf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGenf];
        gl.glTexGenf(i, i1, v);
    }

    @Override
    public void glTexGeni(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGeni];
        gl.glTexGeni(i, i1, i2);
    }

    @Override
    public void glTexGendv(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGendv];
        gl.glTexGendv(i, i1, doubleBuffer);
    }

    @Override
    public void glTexGendv(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGendv];
        gl.glTexGendv(i, i1, doubles, i2);
    }

    @Override
    public void glTexGenfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGenfv];
        gl.glTexGenfv(i, i1, floatBuffer);
    }

    @Override
    public void glTexGenfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGenfv];
        gl.glTexGenfv(i, i1, floats, i2);
    }

    @Override
    public void glTexGeniv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGeniv];
        gl.glTexGeniv(i, i1, intBuffer);
    }

    @Override
    public void glTexGeniv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexGeniv];
        gl.glTexGeniv(i, i1, ints, i2);
    }

    @Override
    public void glGetTexGendv(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGendv];
        gl.glGetTexGendv(i, i1, doubleBuffer);
    }

    @Override
    public void glGetTexGendv(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGendv];
        gl.glGetTexGendv(i, i1, doubles, i2);
    }

    @Override
    public void glGetTexGenfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGenfv];
        gl.glGetTexGenfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetTexGenfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGenfv];
        gl.glGetTexGenfv(i, i1, floats, i2);
    }

    @Override
    public void glGetTexGeniv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGeniv];
        gl.glGetTexGeniv(i, i1, intBuffer);
    }

    @Override
    public void glGetTexGeniv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexGeniv];
        gl.glGetTexGeniv(i, i1, ints, i2);
    }

    @Override
    public void glMap1d(int i, double v, double v1, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap1d];
        gl.glMap1d(i, v, v1, i1, i2, doubleBuffer);
    }

    @Override
    public void glMap1d(int i, double v, double v1, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap1d];
        gl.glMap1d(i, v, v1, i1, i2, doubles, i3);
    }

    @Override
    public void glMap1f(int i, float v, float v1, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap1f];
        gl.glMap1f(i, v, v1, i1, i2, floatBuffer);
    }

    @Override
    public void glMap1f(int i, float v, float v1, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap1f];
        gl.glMap1f(i, v, v1, i1, i2, floats, i3);
    }

    @Override
    public void glMap2d(int i, double v, double v1, int i1, int i2, double v2, double v3, int i3, int i4, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap2d];
        gl.glMap2d(i, v, v1, i1, i2, v2, v3, i3, i4, doubleBuffer);
    }

    @Override
    public void glMap2d(int i, double v, double v1, int i1, int i2, double v2, double v3, int i3, int i4, double[] doubles, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap2d];
        gl.glMap2d(i, v, v1, i1, i2, v2, v3, i3, i4, doubles, i5);
    }

    @Override
    public void glMap2f(int i, float v, float v1, int i1, int i2, float v2, float v3, int i3, int i4, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap2f];
        gl.glMap2f(i, v, v1, i1, i2, v2, v3, i3, i4, floatBuffer);
    }

    @Override
    public void glMap2f(int i, float v, float v1, int i1, int i2, float v2, float v3, int i3, int i4, float[] floats, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glMap2f];
        gl.glMap2f(i, v, v1, i1, i2, v2, v3, i3, i4, floats, i5);
    }

    @Override
    public void glGetMapdv(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapdv];
        gl.glGetMapdv(i, i1, doubleBuffer);
    }

    @Override
    public void glGetMapdv(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapdv];
        gl.glGetMapdv(i, i1, doubles, i2);
    }

    @Override
    public void glGetMapfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapfv];
        gl.glGetMapfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetMapfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapfv];
        gl.glGetMapfv(i, i1, floats, i2);
    }

    @Override
    public void glGetMapiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapiv];
        gl.glGetMapiv(i, i1, intBuffer);
    }

    @Override
    public void glGetMapiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMapiv];
        gl.glGetMapiv(i, i1, ints, i2);
    }

    @Override
    public void glEvalCoord1d(double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1d];
        gl.glEvalCoord1d(v);
    }

    @Override
    public void glEvalCoord1f(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1f];
        gl.glEvalCoord1f(v);
    }

    @Override
    public void glEvalCoord1dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1dv];
        gl.glEvalCoord1dv(doubleBuffer);
    }

    @Override
    public void glEvalCoord1dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1dv];
        gl.glEvalCoord1dv(doubles, i);
    }

    @Override
    public void glEvalCoord1fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1fv];
        gl.glEvalCoord1fv(floatBuffer);
    }

    @Override
    public void glEvalCoord1fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord1fv];
        gl.glEvalCoord1fv(floats, i);
    }

    @Override
    public void glEvalCoord2d(double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2d];
        gl.glEvalCoord2d(v, v1);
    }

    @Override
    public void glEvalCoord2f(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2f];
        gl.glEvalCoord2f(v, v1);
    }

    @Override
    public void glEvalCoord2dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2dv];
        gl.glEvalCoord2dv(doubleBuffer);
    }

    @Override
    public void glEvalCoord2dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2dv];
        gl.glEvalCoord2dv(doubles, i);
    }

    @Override
    public void glEvalCoord2fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2fv];
        gl.glEvalCoord2fv(floatBuffer);
    }

    @Override
    public void glEvalCoord2fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalCoord2fv];
        gl.glEvalCoord2fv(floats, i);
    }

    @Override
    public void glMapGrid1d(int i, double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMapGrid1d];
        gl.glMapGrid1d(i, v, v1);
    }

    @Override
    public void glMapGrid1f(int i, float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMapGrid1f];
        gl.glMapGrid1f(i, v, v1);
    }

    @Override
    public void glMapGrid2d(int i, double v, double v1, int i1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMapGrid2d];
        gl.glMapGrid2d(i, v, v1, i1, v2, v3);
    }

    @Override
    public void glMapGrid2f(int i, float v, float v1, int i1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMapGrid2f];
        gl.glMapGrid2f(i, v, v1, i1, v2, v3);
    }

    @Override
    public void glEvalPoint1(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalPoint1];
        gl.glEvalPoint1(i);
    }

    @Override
    public void glEvalPoint2(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalPoint2];
        gl.glEvalPoint2(i, i1);
    }

    @Override
    public void glEvalMesh1(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalMesh1];
        gl.glEvalMesh1(i, i1, i2);
    }

    @Override
    public void glEvalMesh2(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glEvalMesh2];
        gl.glEvalMesh2(i, i1, i2, i3, i4);
    }

    @Override
    public void glFogi(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogi];
        gl.glFogi(i, i1);
    }

    @Override
    public void glFogiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogiv];
        gl.glFogiv(i, intBuffer);
    }

    @Override
    public void glFogiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogiv];
        gl.glFogiv(i, ints, i1);
    }

    @Override
    public void glFeedbackBuffer(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glFeedbackBuffer];
        gl.glFeedbackBuffer(i, i1, floatBuffer);
    }

    @Override
    public void glPassThrough(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glPassThrough];
        gl.glPassThrough(v);
    }

    @Override
    public void glSelectBuffer(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glSelectBuffer];
        gl.glSelectBuffer(i, intBuffer);
    }

    @Override
    public void glInitNames() {
        verifyAccess();
        ++methodCalls[GLFunc.glInitNames];
        gl.glInitNames();
    }

    @Override
    public void glLoadName(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadName];
        gl.glLoadName(i);
    }

    @Override
    public void glPushName(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glPushName];
        gl.glPushName(i);
    }

    @Override
    public void glPopName() {
        verifyAccess();
        ++methodCalls[GLFunc.glPopName];
        gl.glPopName();
    }

    @Override
    public void glIndexub(byte b) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexub];
        gl.glIndexub(b);
    }

    @Override
    public void glIndexubv(ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexubv];
        gl.glIndexubv(byteBuffer);
    }

    @Override
    public void glIndexubv(byte[] bytes, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexubv];
        gl.glIndexubv(bytes, i);
    }

    @Override
    public void glPushClientAttrib(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glPushClientAttrib];
        gl.glPushClientAttrib(i);
    }

    @Override
    public void glPopClientAttrib() {
        verifyAccess();
        ++methodCalls[GLFunc.glPopClientAttrib];
        gl.glPopClientAttrib();
    }

    @Override
    public void glIndexPointer(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glIndexPointer];
        gl.glIndexPointer(i, i1, buffer);
    }

    @Override
    public void glEdgeFlagPointer(int i, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glEdgeFlagPointer];
        gl.glEdgeFlagPointer(i, buffer);
    }

    @Override
    public void glEdgeFlagPointer(int i, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glEdgeFlagPointer];
        gl.glEdgeFlagPointer(i, l);
    }

    @Override
    public void glArrayElement(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glArrayElement];
        gl.glArrayElement(i);
    }

    @Override
    public void glInterleavedArrays(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glInterleavedArrays];
        gl.glInterleavedArrays(i, i1, buffer);
    }

    @Override
    public void glInterleavedArrays(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glInterleavedArrays];
        gl.glInterleavedArrays(i, i1, l);
    }

    @Override
    public void glPrioritizeTextures(int i, IntBuffer intBuffer, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPrioritizeTextures];
        gl.glPrioritizeTextures(i, intBuffer, floatBuffer);
    }

    @Override
    public void glPrioritizeTextures(int i, int[] ints, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glPrioritizeTextures];
        gl.glPrioritizeTextures(i, ints, i1, floats, i2);
    }

    @Override
    public boolean glAreTexturesResident(int i, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glAreTexturesResident];
        return gl.glAreTexturesResident(i, intBuffer, byteBuffer);
    }

    @Override
    public boolean glAreTexturesResident(int i, int[] ints, int i1, byte[] bytes, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glAreTexturesResident];
        return gl.glAreTexturesResident(i, ints, i1, bytes, i2);
    }

    @Override
    public void glMultiTexCoord1d(int i, double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1d];
        gl.glMultiTexCoord1d(i, v);
    }

    @Override
    public void glMultiTexCoord1dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1dv];
        gl.glMultiTexCoord1dv(i, doubleBuffer);
    }

    @Override
    public void glMultiTexCoord1dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1dv];
        gl.glMultiTexCoord1dv(i, doubles, i1);
    }

    @Override
    public void glMultiTexCoord1f(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1f];
        gl.glMultiTexCoord1f(i, v);
    }

    @Override
    public void glMultiTexCoord1fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1fv];
        gl.glMultiTexCoord1fv(i, floatBuffer);
    }

    @Override
    public void glMultiTexCoord1fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1fv];
        gl.glMultiTexCoord1fv(i, floats, i1);
    }

    @Override
    public void glMultiTexCoord1i(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1i];
        gl.glMultiTexCoord1i(i, i1);
    }

    @Override
    public void glMultiTexCoord1iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1iv];
        gl.glMultiTexCoord1iv(i, intBuffer);
    }

    @Override
    public void glMultiTexCoord1iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1iv];
        gl.glMultiTexCoord1iv(i, ints, i1);
    }

    @Override
    public void glMultiTexCoord1s(int i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1s];
        gl.glMultiTexCoord1s(i, i1);
    }

    @Override
    public void glMultiTexCoord1sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1sv];
        gl.glMultiTexCoord1sv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord1sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1sv];
        gl.glMultiTexCoord1sv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord2d(int i, double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2d];
        gl.glMultiTexCoord2d(i, v, v1);
    }

    @Override
    public void glMultiTexCoord2dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2dv];
        gl.glMultiTexCoord2dv(i, doubleBuffer);
    }

    @Override
    public void glMultiTexCoord2dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2dv];
        gl.glMultiTexCoord2dv(i, doubles, i1);
    }

    @Override
    public void glMultiTexCoord2f(int i, float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2f];
        gl.glMultiTexCoord2f(i, v, v1);
    }

    @Override
    public void glMultiTexCoord2fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2fv];
        gl.glMultiTexCoord2fv(i, floatBuffer);
    }

    @Override
    public void glMultiTexCoord2fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2fv];
        gl.glMultiTexCoord2fv(i, floats, i1);
    }

    @Override
    public void glMultiTexCoord2i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2i];
        gl.glMultiTexCoord2i(i, i1, i2);
    }

    @Override
    public void glMultiTexCoord2iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2iv];
        gl.glMultiTexCoord2iv(i, intBuffer);
    }

    @Override
    public void glMultiTexCoord2iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2iv];
        gl.glMultiTexCoord2iv(i, ints, i1);
    }

    @Override
    public void glMultiTexCoord2s(int i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2s];
        gl.glMultiTexCoord2s(i, i1, i2);
    }

    @Override
    public void glMultiTexCoord2sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2sv];
        gl.glMultiTexCoord2sv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord2sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2sv];
        gl.glMultiTexCoord2sv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord3d(int i, double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3d];
        gl.glMultiTexCoord3d(i, v, v1, v2);
    }

    @Override
    public void glMultiTexCoord3dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3dv];
        gl.glMultiTexCoord3dv(i, doubleBuffer);
    }

    @Override
    public void glMultiTexCoord3dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3dv];
        gl.glMultiTexCoord3dv(i, doubles, i1);
    }

    @Override
    public void glMultiTexCoord3f(int i, float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3f];
        gl.glMultiTexCoord3f(i, v, v1, v2);
    }

    @Override
    public void glMultiTexCoord3fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3fv];
        gl.glMultiTexCoord3fv(i, floatBuffer);
    }

    @Override
    public void glMultiTexCoord3fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3fv];
        gl.glMultiTexCoord3fv(i, floats, i1);
    }

    @Override
    public void glMultiTexCoord3i(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3i];
        gl.glMultiTexCoord3i(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexCoord3iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3iv];
        gl.glMultiTexCoord3iv(i, intBuffer);
    }

    @Override
    public void glMultiTexCoord3iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3iv];
        gl.glMultiTexCoord3iv(i, ints, i1);
    }

    @Override
    public void glMultiTexCoord3s(int i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3s];
        gl.glMultiTexCoord3s(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexCoord3sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3sv];
        gl.glMultiTexCoord3sv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord3sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3sv];
        gl.glMultiTexCoord3sv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord4d(int i, double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4d];
        gl.glMultiTexCoord4d(i, v, v1, v2, v3);
    }

    @Override
    public void glMultiTexCoord4dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4dv];
        gl.glMultiTexCoord4dv(i, doubleBuffer);
    }

    @Override
    public void glMultiTexCoord4dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4dv];
        gl.glMultiTexCoord4dv(i, doubles, i1);
    }

    @Override
    public void glMultiTexCoord4fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4fv];
        gl.glMultiTexCoord4fv(i, floatBuffer);
    }

    @Override
    public void glMultiTexCoord4fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4fv];
        gl.glMultiTexCoord4fv(i, floats, i1);
    }

    @Override
    public void glMultiTexCoord4i(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4i];
        gl.glMultiTexCoord4i(i, i1, i2, i3, i4);
    }

    @Override
    public void glMultiTexCoord4iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4iv];
        gl.glMultiTexCoord4iv(i, intBuffer);
    }

    @Override
    public void glMultiTexCoord4iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4iv];
        gl.glMultiTexCoord4iv(i, ints, i1);
    }

    @Override
    public void glMultiTexCoord4s(int i, short i1, short i2, short i3, short i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4s];
        gl.glMultiTexCoord4s(i, i1, i2, i3, i4);
    }

    @Override
    public void glMultiTexCoord4sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4sv];
        gl.glMultiTexCoord4sv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord4sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4sv];
        gl.glMultiTexCoord4sv(i, shorts, i1);
    }

    @Override
    public void glLoadTransposeMatrixf(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadTransposeMatrixf];
        gl.glLoadTransposeMatrixf(floatBuffer);
    }

    @Override
    public void glLoadTransposeMatrixf(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadTransposeMatrixf];
        gl.glLoadTransposeMatrixf(floats, i);
    }

    @Override
    public void glLoadTransposeMatrixd(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadTransposeMatrixd];
        gl.glLoadTransposeMatrixd(doubleBuffer);
    }

    @Override
    public void glLoadTransposeMatrixd(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadTransposeMatrixd];
        gl.glLoadTransposeMatrixd(doubles, i);
    }

    @Override
    public void glMultTransposeMatrixf(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultTransposeMatrixf];
        gl.glMultTransposeMatrixf(floatBuffer);
    }

    @Override
    public void glMultTransposeMatrixf(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultTransposeMatrixf];
        gl.glMultTransposeMatrixf(floats, i);
    }

    @Override
    public void glMultTransposeMatrixd(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultTransposeMatrixd];
        gl.glMultTransposeMatrixd(doubleBuffer);
    }

    @Override
    public void glMultTransposeMatrixd(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultTransposeMatrixd];
        gl.glMultTransposeMatrixd(doubles, i);
    }

    @Override
    public void glFogCoordf(float v) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordf];
        gl.glFogCoordf(v);
    }

    @Override
    public void glFogCoordfv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordfv];
        gl.glFogCoordfv(floatBuffer);
    }

    @Override
    public void glFogCoordfv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordfv];
        gl.glFogCoordfv(floats, i);
    }

    @Override
    public void glFogCoordd(double v) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordd];
        gl.glFogCoordd(v);
    }

    @Override
    public void glFogCoorddv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoorddv];
        gl.glFogCoorddv(doubleBuffer);
    }

    @Override
    public void glFogCoorddv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoorddv];
        gl.glFogCoorddv(doubles, i);
    }

    @Override
    public void glFogCoordPointer(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogCoordPointer];
        gl.glFogCoordPointer(i, i1, buffer);
    }

    @Override
    public void glFogCoordPointer(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogCoordPointer];
        gl.glFogCoordPointer(i, i1, l);
    }

    @Override
    public void glSecondaryColor3b(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3b];
        gl.glSecondaryColor3b(b, b1, b2);
    }

    @Override
    public void glSecondaryColor3bv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3bv];
        gl.glSecondaryColor3bv(byteBuffer);
    }

    @Override
    public void glSecondaryColor3bv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3bv];
        gl.glSecondaryColor3bv(bytes, i);
    }

    @Override
    public void glSecondaryColor3d(double v, double v1, double v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3d];
        gl.glSecondaryColor3d(v, v1, v2);
    }

    @Override
    public void glSecondaryColor3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3dv];
        gl.glSecondaryColor3dv(doubleBuffer);
    }

    @Override
    public void glSecondaryColor3dv(double[] doubles, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3dv];
        gl.glSecondaryColor3dv(doubles, i);
    }

    @Override
    public void glSecondaryColor3f(float v, float v1, float v2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3f];
        gl.glSecondaryColor3f(v, v1, v2);
    }

    @Override
    public void glSecondaryColor3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3fv];
        gl.glSecondaryColor3fv(floatBuffer);
    }

    @Override
    public void glSecondaryColor3fv(float[] floats, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3fv];
        gl.glSecondaryColor3fv(floats, i);
    }

    @Override
    public void glSecondaryColor3i(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3i];
        gl.glSecondaryColor3i(i, i1, i2);
    }

    @Override
    public void glSecondaryColor3iv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3iv];
        gl.glSecondaryColor3iv(intBuffer);
    }

    @Override
    public void glSecondaryColor3iv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3iv];
        gl.glSecondaryColor3iv(ints, i);
    }

    @Override
    public void glSecondaryColor3s(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3s];
        gl.glSecondaryColor3s(i, i1, i2);
    }

    @Override
    public void glSecondaryColor3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3sv];
        gl.glSecondaryColor3sv(shortBuffer);
    }

    @Override
    public void glSecondaryColor3sv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3sv];
        gl.glSecondaryColor3sv(shorts, i);
    }

    @Override
    public void glSecondaryColor3ub(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3ub];
        gl.glSecondaryColor3ub(b, b1, b2);
    }

    @Override
    public void glSecondaryColor3ubv(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3ubv];
        gl.glSecondaryColor3ubv(byteBuffer);
    }

    @Override
    public void glSecondaryColor3ubv(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3ubv];
        gl.glSecondaryColor3ubv(bytes, i);
    }

    @Override
    public void glSecondaryColor3ui(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3ui];
        gl.glSecondaryColor3ui(i, i1, i2);
    }

    @Override
    public void glSecondaryColor3uiv(IntBuffer intBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3uiv];
        gl.glSecondaryColor3uiv(intBuffer);
    }

    @Override
    public void glSecondaryColor3uiv(int[] ints, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3uiv];
        gl.glSecondaryColor3uiv(ints, i);
    }

    @Override
    public void glSecondaryColor3us(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3us];
        gl.glSecondaryColor3us(i, i1, i2);
    }

    @Override
    public void glSecondaryColor3usv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3usv];
        gl.glSecondaryColor3usv(shortBuffer);
    }

    @Override
    public void glSecondaryColor3usv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3usv];
        gl.glSecondaryColor3usv(shorts, i);
    }

    @Override
    public void glSecondaryColorPointer(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glSecondaryColorPointer];
        gl.glSecondaryColorPointer(i, i1, i2, buffer);
    }

    @Override
    public void glSecondaryColorPointer(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glSecondaryColorPointer];
        gl.glSecondaryColorPointer(i, i1, i2, l);
    }

    @Override
    public void glWindowPos2d(double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2d];
        gl.glWindowPos2d(v, v1);
    }

    @Override
    public void glWindowPos2dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2dv];
        gl.glWindowPos2dv(doubleBuffer);
    }

    @Override
    public void glWindowPos2dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2dv];
        gl.glWindowPos2dv(doubles, i);
    }

    @Override
    public void glWindowPos2f(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2f];
        gl.glWindowPos2f(v, v1);
    }

    @Override
    public void glWindowPos2fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2fv];
        gl.glWindowPos2fv(floatBuffer);
    }

    @Override
    public void glWindowPos2fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2fv];
        gl.glWindowPos2fv(floats, i);
    }

    @Override
    public void glWindowPos2i(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2i];
        gl.glWindowPos2i(i, i1);
    }

    @Override
    public void glWindowPos2iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2iv];
        gl.glWindowPos2iv(intBuffer);
    }

    @Override
    public void glWindowPos2iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2iv];
        gl.glWindowPos2iv(ints, i);
    }

    @Override
    public void glWindowPos2s(short i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2s];
        gl.glWindowPos2s(i, i1);
    }

    @Override
    public void glWindowPos2sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2sv];
        gl.glWindowPos2sv(shortBuffer);
    }

    @Override
    public void glWindowPos2sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos2sv];
        gl.glWindowPos2sv(shorts, i);
    }

    @Override
    public void glWindowPos3d(double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3d];
        gl.glWindowPos3d(v, v1, v2);
    }

    @Override
    public void glWindowPos3dv(DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3dv];
        gl.glWindowPos3dv(doubleBuffer);
    }

    @Override
    public void glWindowPos3dv(double[] doubles, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3dv];
        gl.glWindowPos3dv(doubles, i);
    }

    @Override
    public void glWindowPos3f(float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3f];
        gl.glWindowPos3f(v, v1, v2);
    }

    @Override
    public void glWindowPos3fv(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3fv];
        gl.glWindowPos3fv(floatBuffer);
    }

    @Override
    public void glWindowPos3fv(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3fv];
        gl.glWindowPos3fv(floats, i);
    }

    @Override
    public void glWindowPos3i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3i];
        gl.glWindowPos3i(i, i1, i2);
    }

    @Override
    public void glWindowPos3iv(IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3iv];
        gl.glWindowPos3iv(intBuffer);
    }

    @Override
    public void glWindowPos3iv(int[] ints, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3iv];
        gl.glWindowPos3iv(ints, i);
    }

    @Override
    public void glWindowPos3s(short i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3s];
        gl.glWindowPos3s(i, i1, i2);
    }

    @Override
    public void glWindowPos3sv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3sv];
        gl.glWindowPos3sv(shortBuffer);
    }

    @Override
    public void glWindowPos3sv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glWindowPos3sv];
        gl.glWindowPos3sv(shorts, i);
    }

    @Override
    public void glClearNamedBufferData(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearNamedBufferData];
        gl.glClearNamedBufferData(i, i1, i2, i3, buffer);
    }

    @Override
    public void glClearNamedBufferSubData(int i, int i1, long l, long l1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearNamedBufferSubData];
        gl.glClearNamedBufferSubData(i, i1, l, l1, i2, i3, buffer);
    }

    @Override
    public void glNamedFramebufferParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNamedFramebufferParameteri];
        gl.glNamedFramebufferParameteri(i, i1, i2);
    }

    @Override
    public void glGetNamedFramebufferParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetNamedFramebufferParameteriv];
        gl.glGetNamedFramebufferParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetNamedFramebufferParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetNamedFramebufferParameteriv];
        gl.glGetNamedFramebufferParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glGetnMapdv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapdv];
        gl.glGetnMapdv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glGetnMapdv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapdv];
        gl.glGetnMapdv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glGetnMapfv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapfv];
        gl.glGetnMapfv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetnMapfv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapfv];
        gl.glGetnMapfv(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetnMapiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapiv];
        gl.glGetnMapiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetnMapiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMapiv];
        gl.glGetnMapiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetnPixelMapfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapfv];
        gl.glGetnPixelMapfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetnPixelMapfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapfv];
        gl.glGetnPixelMapfv(i, i1, floats, i2);
    }

    @Override
    public void glGetnPixelMapuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapuiv];
        gl.glGetnPixelMapuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetnPixelMapuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapuiv];
        gl.glGetnPixelMapuiv(i, i1, ints, i2);
    }

    @Override
    public void glGetnPixelMapusv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapusv];
        gl.glGetnPixelMapusv(i, i1, shortBuffer);
    }

    @Override
    public void glGetnPixelMapusv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPixelMapusv];
        gl.glGetnPixelMapusv(i, i1, shorts, i2);
    }

    @Override
    public void glGetnPolygonStipple(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPolygonStipple];
        gl.glGetnPolygonStipple(i, byteBuffer);
    }

    @Override
    public void glGetnPolygonStipple(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnPolygonStipple];
        gl.glGetnPolygonStipple(i, bytes, i1);
    }

    @Override
    public void glGetnColorTable(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnColorTable];
        gl.glGetnColorTable(i, i1, i2, i3, buffer);
    }

    @Override
    public void glGetnConvolutionFilter(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnConvolutionFilter];
        gl.glGetnConvolutionFilter(i, i1, i2, i3, buffer);
    }

    @Override
    public void glGetnSeparableFilter(int i, int i1, int i2, int i3, Buffer buffer, int i4, Buffer buffer1, Buffer buffer2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnSeparableFilter];
        gl.glGetnSeparableFilter(i, i1, i2, i3, buffer, i4, buffer1, buffer2);
    }

    @Override
    public void glGetnHistogram(int i, boolean b, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnHistogram];
        gl.glGetnHistogram(i, b, i1, i2, i3, buffer);
    }

    @Override
    public void glGetnMinmax(int i, boolean b, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnMinmax];
        gl.glGetnMinmax(i, b, i1, i2, i3, buffer);
    }

    @Override
    public void glProgramStringARB(int i, int i1, int i2, String s) {
        verifyAccess();
        gl.glProgramStringARB(i, i1, i2, s);
    }

    @Override
    public void glBindProgramARB(int i, int i1) {
        verifyAccess();
        gl.glBindProgramARB(i, i1);
    }

    @Override
    public void glDeleteProgramsARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteProgramsARB(i, intBuffer);
    }

    @Override
    public void glDeleteProgramsARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeleteProgramsARB(i, ints, i1);
    }

    @Override
    public void glGenProgramsARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGenProgramsARB(i, intBuffer);
    }

    @Override
    public void glGenProgramsARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGenProgramsARB(i, ints, i1);
    }

    @Override
    public void glProgramEnvParameter4dARB(int i, int i1, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glProgramEnvParameter4dARB(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramEnvParameter4dvARB(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramEnvParameter4dvARB(i, i1, doubleBuffer);
    }

    @Override
    public void glProgramEnvParameter4dvARB(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glProgramEnvParameter4dvARB(i, i1, doubles, i2);
    }

    @Override
    public void glProgramEnvParameter4fARB(int i, int i1, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glProgramEnvParameter4fARB(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramEnvParameter4fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glProgramEnvParameter4fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glProgramEnvParameter4fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glProgramEnvParameter4fvARB(i, i1, floats, i2);
    }

    @Override
    public void glProgramLocalParameter4dARB(int i, int i1, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glProgramLocalParameter4dARB(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramLocalParameter4dvARB(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramLocalParameter4dvARB(i, i1, doubleBuffer);
    }

    @Override
    public void glProgramLocalParameter4dvARB(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glProgramLocalParameter4dvARB(i, i1, doubles, i2);
    }

    @Override
    public void glProgramLocalParameter4fARB(int i, int i1, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glProgramLocalParameter4fARB(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramLocalParameter4fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glProgramLocalParameter4fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glProgramLocalParameter4fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glProgramLocalParameter4fvARB(i, i1, floats, i2);
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetProgramEnvParameterdvARB(i, i1, doubleBuffer);
    }

    @Override
    public void glGetProgramEnvParameterdvARB(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glGetProgramEnvParameterdvARB(i, i1, doubles, i2);
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetProgramEnvParameterfvARB(i, i1, floatBuffer);
    }

    @Override
    public void glGetProgramEnvParameterfvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetProgramEnvParameterfvARB(i, i1, floats, i2);
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetProgramLocalParameterdvARB(i, i1, doubleBuffer);
    }

    @Override
    public void glGetProgramLocalParameterdvARB(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glGetProgramLocalParameterdvARB(i, i1, doubles, i2);
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetProgramLocalParameterfvARB(i, i1, floatBuffer);
    }

    @Override
    public void glGetProgramLocalParameterfvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetProgramLocalParameterfvARB(i, i1, floats, i2);
    }

    @Override
    public void glGetProgramivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramivARB(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramivARB(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramStringARB(int i, int i1, Buffer buffer) {
        verifyAccess();
        gl.glGetProgramStringARB(i, i1, buffer);
    }

    @Override
    public boolean glIsProgramARB(int i) {
        verifyAccess();
        return gl.glIsProgramARB(i);
    }

    @Override
    public void glUniform1i64ARB(int i, long l) {
        verifyAccess();
        gl.glUniform1i64ARB(i, l);
    }

    @Override
    public void glUniform2i64ARB(int i, long l, long l1) {
        verifyAccess();
        gl.glUniform2i64ARB(i, l, l1);
    }

    @Override
    public void glUniform3i64ARB(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glUniform3i64ARB(i, l, l1, l2);
    }

    @Override
    public void glUniform4i64ARB(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glUniform4i64ARB(i, l, l1, l2, l3);
    }

    @Override
    public void glUniform1i64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform1i64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform1i64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform1i64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform2i64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform2i64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform2i64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform2i64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform3i64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform3i64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform3i64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform3i64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform4i64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform4i64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform4i64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform4i64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform1ui64ARB(int i, long l) {
        verifyAccess();
        gl.glUniform1ui64ARB(i, l);
    }

    @Override
    public void glUniform2ui64ARB(int i, long l, long l1) {
        verifyAccess();
        gl.glUniform2ui64ARB(i, l, l1);
    }

    @Override
    public void glUniform3ui64ARB(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glUniform3ui64ARB(i, l, l1, l2);
    }

    @Override
    public void glUniform4ui64ARB(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glUniform4ui64ARB(i, l, l1, l2, l3);
    }

    @Override
    public void glUniform1ui64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform1ui64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform1ui64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform1ui64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform2ui64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform2ui64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform2ui64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform2ui64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform3ui64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform3ui64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform3ui64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform3ui64vARB(i, i1, longs, i2);
    }

    @Override
    public void glUniform4ui64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform4ui64vARB(i, i1, longBuffer);
    }

    @Override
    public void glUniform4ui64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform4ui64vARB(i, i1, longs, i2);
    }

    @Override
    public void glGetUniformi64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetUniformi64vARB(i, i1, longBuffer);
    }

    @Override
    public void glGetUniformi64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetUniformi64vARB(i, i1, longs, i2);
    }

    @Override
    public void glGetUniformui64vARB(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetUniformui64vARB(i, i1, longBuffer);
    }

    @Override
    public void glGetUniformui64vARB(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetUniformui64vARB(i, i1, longs, i2);
    }

    @Override
    public void glGetnUniformi64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetnUniformi64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glGetnUniformi64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glGetnUniformi64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glGetnUniformui64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetnUniformui64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glGetnUniformui64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glGetnUniformui64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform1i64ARB(int i, int i1, long l) {
        verifyAccess();
        gl.glProgramUniform1i64ARB(i, i1, l);
    }

    @Override
    public void glProgramUniform2i64ARB(int i, int i1, long l, long l1) {
        verifyAccess();
        gl.glProgramUniform2i64ARB(i, i1, l, l1);
    }

    @Override
    public void glProgramUniform3i64ARB(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        gl.glProgramUniform3i64ARB(i, i1, l, l1, l2);
    }

    @Override
    public void glProgramUniform4i64ARB(int i, int i1, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glProgramUniform4i64ARB(i, i1, l, l1, l2, l3);
    }

    @Override
    public void glProgramUniform1i64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform1i64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform1i64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform1i64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform2i64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform2i64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform2i64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform2i64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform3i64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform3i64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform3i64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform3i64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform4i64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform4i64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform4i64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform4i64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform1ui64ARB(int i, int i1, long l) {
        verifyAccess();
        gl.glProgramUniform1ui64ARB(i, i1, l);
    }

    @Override
    public void glProgramUniform2ui64ARB(int i, int i1, long l, long l1) {
        verifyAccess();
        gl.glProgramUniform2ui64ARB(i, i1, l, l1);
    }

    @Override
    public void glProgramUniform3ui64ARB(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        gl.glProgramUniform3ui64ARB(i, i1, l, l1, l2);
    }

    @Override
    public void glProgramUniform4ui64ARB(int i, int i1, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glProgramUniform4ui64ARB(i, i1, l, l1, l2, l3);
    }

    @Override
    public void glProgramUniform1ui64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform1ui64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform1ui64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform1ui64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform2ui64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform2ui64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform2ui64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform2ui64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform3ui64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform3ui64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform3ui64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform3ui64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform4ui64vARB(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform4ui64vARB(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform4ui64vARB(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform4ui64vARB(i, i1, i2, longs, i3);
    }

    @Override
    public void glColorTable(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTable];
        gl.glColorTable(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glColorTable(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTable];
        gl.glColorTable(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glColorTableParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTableParameterfv];
        gl.glColorTableParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glColorTableParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTableParameterfv];
        gl.glColorTableParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glColorTableParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTableParameteriv];
        gl.glColorTableParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glColorTableParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorTableParameteriv];
        gl.glColorTableParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glCopyColorTable(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyColorTable];
        gl.glCopyColorTable(i, i1, i2, i3, i4);
    }

    @Override
    public void glGetColorTable(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTable];
        gl.glGetColorTable(i, i1, i2, buffer);
    }

    @Override
    public void glGetColorTable(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTable];
        gl.glGetColorTable(i, i1, i2, l);
    }

    @Override
    public void glGetColorTableParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTableParameterfv];
        gl.glGetColorTableParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetColorTableParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTableParameterfv];
        gl.glGetColorTableParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glGetColorTableParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTableParameteriv];
        gl.glGetColorTableParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetColorTableParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetColorTableParameteriv];
        gl.glGetColorTableParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glColorSubTable(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorSubTable];
        gl.glColorSubTable(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glColorSubTable(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorSubTable];
        gl.glColorSubTable(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glCopyColorSubTable(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyColorSubTable];
        gl.glCopyColorSubTable(i, i1, i2, i3, i4);
    }

    @Override
    public void glConvolutionFilter1D(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionFilter1D];
        gl.glConvolutionFilter1D(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glConvolutionFilter1D(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionFilter1D];
        gl.glConvolutionFilter1D(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glConvolutionFilter2D(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionFilter2D];
        gl.glConvolutionFilter2D(i, i1, i2, i3, i4, i5, buffer);
    }

    @Override
    public void glConvolutionFilter2D(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionFilter2D];
        gl.glConvolutionFilter2D(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glConvolutionParameterf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameterf];
        gl.glConvolutionParameterf(i, i1, v);
    }

    @Override
    public void glConvolutionParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameterfv];
        gl.glConvolutionParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glConvolutionParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameterfv];
        gl.glConvolutionParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glConvolutionParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameteri];
        gl.glConvolutionParameteri(i, i1, i2);
    }

    @Override
    public void glConvolutionParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameteriv];
        gl.glConvolutionParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glConvolutionParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glConvolutionParameteriv];
        gl.glConvolutionParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glCopyConvolutionFilter1D(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyConvolutionFilter1D];
        gl.glCopyConvolutionFilter1D(i, i1, i2, i3, i4);
    }

    @Override
    public void glCopyConvolutionFilter2D(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyConvolutionFilter2D];
        gl.glCopyConvolutionFilter2D(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glGetConvolutionFilter(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionFilter];
        gl.glGetConvolutionFilter(i, i1, i2, buffer);
    }

    @Override
    public void glGetConvolutionFilter(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionFilter];
        gl.glGetConvolutionFilter(i, i1, i2, l);
    }

    @Override
    public void glGetConvolutionParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionParameterfv];
        gl.glGetConvolutionParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetConvolutionParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionParameterfv];
        gl.glGetConvolutionParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glGetConvolutionParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionParameteriv];
        gl.glGetConvolutionParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetConvolutionParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetConvolutionParameteriv];
        gl.glGetConvolutionParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glGetSeparableFilter(int i, int i1, int i2, Buffer buffer, Buffer buffer1, Buffer buffer2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSeparableFilter];
        gl.glGetSeparableFilter(i, i1, i2, buffer, buffer1, buffer2);
    }

    @Override
    public void glGetSeparableFilter(int i, int i1, int i2, long l, long l1, long l2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSeparableFilter];
        gl.glGetSeparableFilter(i, i1, i2, l, l1, l2);
    }

    @Override
    public void glSeparableFilter2D(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer, Buffer buffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glSeparableFilter2D];
        gl.glSeparableFilter2D(i, i1, i2, i3, i4, i5, buffer, buffer1);
    }

    @Override
    public void glSeparableFilter2D(int i, int i1, int i2, int i3, int i4, int i5, long l, long l1) {
        verifyAccess();
        ++methodCalls[GLFunc.glSeparableFilter2D];
        gl.glSeparableFilter2D(i, i1, i2, i3, i4, i5, l, l1);
    }

    @Override
    public void glGetHistogram(int i, boolean b, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogram];
        gl.glGetHistogram(i, b, i1, i2, buffer);
    }

    @Override
    public void glGetHistogram(int i, boolean b, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogram];
        gl.glGetHistogram(i, b, i1, i2, l);
    }

    @Override
    public void glGetHistogramParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogramParameterfv];
        gl.glGetHistogramParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetHistogramParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogramParameterfv];
        gl.glGetHistogramParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glGetHistogramParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogramParameteriv];
        gl.glGetHistogramParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetHistogramParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetHistogramParameteriv];
        gl.glGetHistogramParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glGetMinmax(int i, boolean b, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmax];
        gl.glGetMinmax(i, b, i1, i2, buffer);
    }

    @Override
    public void glGetMinmax(int i, boolean b, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmax];
        gl.glGetMinmax(i, b, i1, i2, l);
    }

    @Override
    public void glGetMinmaxParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmaxParameterfv];
        gl.glGetMinmaxParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetMinmaxParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmaxParameterfv];
        gl.glGetMinmaxParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glGetMinmaxParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmaxParameteriv];
        gl.glGetMinmaxParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetMinmaxParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMinmaxParameteriv];
        gl.glGetMinmaxParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glHistogram(int i, int i1, int i2, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glHistogram];
        gl.glHistogram(i, i1, i2, b);
    }

    @Override
    public void glMinmax(int i, int i1, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glMinmax];
        gl.glMinmax(i, i1, b);
    }

    @Override
    public void glResetHistogram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glResetHistogram];
        gl.glResetHistogram(i);
    }

    @Override
    public void glResetMinmax(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glResetMinmax];
        gl.glResetMinmax(i);
    }

    @Override
    public void glCurrentPaletteMatrixARB(int i) {
        verifyAccess();
        gl.glCurrentPaletteMatrixARB(i);
    }

    @Override
    public void glMatrixIndexubvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glMatrixIndexubvARB(i, byteBuffer);
    }

    @Override
    public void glMatrixIndexubvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glMatrixIndexubvARB(i, bytes, i1);
    }

    @Override
    public void glMatrixIndexusvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glMatrixIndexusvARB(i, shortBuffer);
    }

    @Override
    public void glMatrixIndexusvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glMatrixIndexusvARB(i, shorts, i1);
    }

    @Override
    public void glMatrixIndexuivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMatrixIndexuivARB(i, intBuffer);
    }

    @Override
    public void glMatrixIndexuivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glMatrixIndexuivARB(i, ints, i1);
    }

    @Override
    public void glMatrixIndexPointerARB(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glMatrixIndexPointerARB(i, i1, i2, buffer);
    }

    @Override
    public void glMatrixIndexPointerARB(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glMatrixIndexPointerARB(i, i1, i2, l);
    }

    @Override
    public void glMaxShaderCompilerThreadsARB(int i) {
        verifyAccess();
        gl.glMaxShaderCompilerThreadsARB(i);
    }

    @Override
    public void glFramebufferSampleLocationsfvARB(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glFramebufferSampleLocationsfvARB(i, i1, i2, floatBuffer);
    }

    @Override
    public void glFramebufferSampleLocationsfvARB(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glFramebufferSampleLocationsfvARB(i, i1, i2, floats, i3);
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvARB(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glNamedFramebufferSampleLocationsfvARB(i, i1, i2, floatBuffer);
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvARB(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glNamedFramebufferSampleLocationsfvARB(i, i1, i2, floats, i3);
    }

    @Override
    public void glEvaluateDepthValuesARB() {
        verifyAccess();
        gl.glEvaluateDepthValuesARB();
    }

    @Override
    public void glDeleteObjectARB(long l) {
        verifyAccess();
        gl.glDeleteObjectARB(l);
    }

    @Override
    public long glGetHandleARB(int i) {
        verifyAccess();
        return gl.glGetHandleARB(i);
    }

    @Override
    public void glDetachObjectARB(long l, long l1) {
        verifyAccess();
        gl.glDetachObjectARB(l, l1);
    }

    @Override
    public long glCreateShaderObjectARB(int i) {
        verifyAccess();
        return gl.glCreateShaderObjectARB(i);
    }

    @Override
    public void glShaderSourceARB(long l, int i, String[] strings, IntBuffer intBuffer) {
        verifyAccess();
        gl.glShaderSourceARB(l, i, strings, intBuffer);
    }

    @Override
    public void glShaderSourceARB(long l, int i, String[] strings, int[] ints, int i1) {
        verifyAccess();
        gl.glShaderSourceARB(l, i, strings, ints, i1);
    }

    @Override
    public void glCompileShaderARB(long l) {
        verifyAccess();
        gl.glCompileShaderARB(l);
    }

    @Override
    public long glCreateProgramObjectARB() {
        verifyAccess();
        return gl.glCreateProgramObjectARB();
    }

    @Override
    public void glAttachObjectARB(long l, long l1) {
        verifyAccess();
        gl.glAttachObjectARB(l, l1);
    }

    @Override
    public void glLinkProgramARB(long l) {
        verifyAccess();
        gl.glLinkProgramARB(l);
    }

    @Override
    public void glUseProgramObjectARB(long l) {
        verifyAccess();
        gl.glUseProgramObjectARB(l);
    }

    @Override
    public void glValidateProgramARB(long l) {
        verifyAccess();
        gl.glValidateProgramARB(l);
    }

    @Override
    public void glUniform1fARB(int i, float v) {
        verifyAccess();
        gl.glUniform1fARB(i, v);
    }

    @Override
    public void glUniform2fARB(int i, float v, float v1) {
        verifyAccess();
        gl.glUniform2fARB(i, v, v1);
    }

    @Override
    public void glUniform3fARB(int i, float v, float v1, float v2) {
        verifyAccess();
        gl.glUniform3fARB(i, v, v1, v2);
    }

    @Override
    public void glUniform4fARB(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glUniform4fARB(i, v, v1, v2, v3);
    }

    @Override
    public void glUniform1iARB(int i, int i1) {
        verifyAccess();
        gl.glUniform1iARB(i, i1);
    }

    @Override
    public void glUniform2iARB(int i, int i1, int i2) {
        verifyAccess();
        gl.glUniform2iARB(i, i1, i2);
    }

    @Override
    public void glUniform3iARB(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glUniform3iARB(i, i1, i2, i3);
    }

    @Override
    public void glUniform4iARB(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glUniform4iARB(i, i1, i2, i3, i4);
    }

    @Override
    public void glUniform1fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniform1fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glUniform1fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glUniform1fvARB(i, i1, floats, i2);
    }

    @Override
    public void glUniform2fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniform2fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glUniform2fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glUniform2fvARB(i, i1, floats, i2);
    }

    @Override
    public void glUniform3fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniform3fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glUniform3fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glUniform3fvARB(i, i1, floats, i2);
    }

    @Override
    public void glUniform4fvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniform4fvARB(i, i1, floatBuffer);
    }

    @Override
    public void glUniform4fvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glUniform4fvARB(i, i1, floats, i2);
    }

    @Override
    public void glUniform1ivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glUniform1ivARB(i, i1, intBuffer);
    }

    @Override
    public void glUniform1ivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glUniform1ivARB(i, i1, ints, i2);
    }

    @Override
    public void glUniform2ivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glUniform2ivARB(i, i1, intBuffer);
    }

    @Override
    public void glUniform2ivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glUniform2ivARB(i, i1, ints, i2);
    }

    @Override
    public void glUniform3ivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glUniform3ivARB(i, i1, intBuffer);
    }

    @Override
    public void glUniform3ivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glUniform3ivARB(i, i1, ints, i2);
    }

    @Override
    public void glUniform4ivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glUniform4ivARB(i, i1, intBuffer);
    }

    @Override
    public void glUniform4ivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glUniform4ivARB(i, i1, ints, i2);
    }

    @Override
    public void glUniformMatrix2fvARB(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniformMatrix2fvARB(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix2fvARB(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        gl.glUniformMatrix2fvARB(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix3fvARB(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniformMatrix3fvARB(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix3fvARB(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        gl.glUniformMatrix3fvARB(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix4fvARB(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glUniformMatrix4fvARB(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix4fvARB(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        gl.glUniformMatrix4fvARB(i, i1, b, floats, i2);
    }

    @Override
    public void glGetObjectParameterfvARB(long l, int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetObjectParameterfvARB(l, i, floatBuffer);
    }

    @Override
    public void glGetObjectParameterfvARB(long l, int i, float[] floats, int i1) {
        verifyAccess();
        gl.glGetObjectParameterfvARB(l, i, floats, i1);
    }

    @Override
    public void glGetObjectParameterivARB(long l, int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetObjectParameterivARB(l, i, intBuffer);
    }

    @Override
    public void glGetObjectParameterivARB(long l, int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGetObjectParameterivARB(l, i, ints, i1);
    }

    @Override
    public void glGetInfoLogARB(long l, int i, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetInfoLogARB(l, i, intBuffer, byteBuffer);
    }

    @Override
    public void glGetInfoLogARB(long l, int i, int[] ints, int i1, byte[] bytes, int i2) {
        verifyAccess();
        gl.glGetInfoLogARB(l, i, ints, i1, bytes, i2);
    }

    @Override
    public void glGetAttachedObjectsARB(long l, int i, IntBuffer intBuffer, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetAttachedObjectsARB(l, i, intBuffer, longBuffer);
    }

    @Override
    public void glGetAttachedObjectsARB(long l, int i, int[] ints, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetAttachedObjectsARB(l, i, ints, i1, longs, i2);
    }

    @Override
    public int glGetUniformLocationARB(long l, String s) {
        verifyAccess();
        return gl.glGetUniformLocationARB(l, s);
    }

    @Override
    public void glGetActiveUniformARB(long l, int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetActiveUniformARB(l, i, i1, intBuffer, intBuffer1, intBuffer2, byteBuffer);
    }

    @Override
    public void glGetActiveUniformARB(long l, int i, int i1, int[] ints, int i2, int[] ints1, int i3, int[] ints2, int i4, byte[] bytes, int i5) {
        verifyAccess();
        gl.glGetActiveUniformARB(l, i, i1, ints, i2, ints1, i3, ints2, i4, bytes, i5);
    }

    @Override
    public void glGetUniformfvARB(long l, int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetUniformfvARB(l, i, floatBuffer);
    }

    @Override
    public void glGetUniformfvARB(long l, int i, float[] floats, int i1) {
        verifyAccess();
        gl.glGetUniformfvARB(l, i, floats, i1);
    }

    @Override
    public void glGetUniformivARB(long l, int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetUniformivARB(l, i, intBuffer);
    }

    @Override
    public void glGetUniformivARB(long l, int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGetUniformivARB(l, i, ints, i1);
    }

    @Override
    public void glGetShaderSourceARB(long l, int i, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetShaderSourceARB(l, i, intBuffer, byteBuffer);
    }

    @Override
    public void glGetShaderSourceARB(long l, int i, int[] ints, int i1, byte[] bytes, int i2) {
        verifyAccess();
        gl.glGetShaderSourceARB(l, i, ints, i1, bytes, i2);
    }

    @Override
    public void glWeightbvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glWeightbvARB(i, byteBuffer);
    }

    @Override
    public void glWeightbvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glWeightbvARB(i, bytes, i1);
    }

    @Override
    public void glWeightsvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glWeightsvARB(i, shortBuffer);
    }

    @Override
    public void glWeightsvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glWeightsvARB(i, shorts, i1);
    }

    @Override
    public void glWeightivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glWeightivARB(i, intBuffer);
    }

    @Override
    public void glWeightivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glWeightivARB(i, ints, i1);
    }

    @Override
    public void glWeightfvARB(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glWeightfvARB(i, floatBuffer);
    }

    @Override
    public void glWeightfvARB(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glWeightfvARB(i, floats, i1);
    }

    @Override
    public void glWeightdvARB(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glWeightdvARB(i, doubleBuffer);
    }

    @Override
    public void glWeightdvARB(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glWeightdvARB(i, doubles, i1);
    }

    @Override
    public void glWeightubvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glWeightubvARB(i, byteBuffer);
    }

    @Override
    public void glWeightubvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glWeightubvARB(i, bytes, i1);
    }

    @Override
    public void glWeightusvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glWeightusvARB(i, shortBuffer);
    }

    @Override
    public void glWeightusvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glWeightusvARB(i, shorts, i1);
    }

    @Override
    public void glWeightuivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glWeightuivARB(i, intBuffer);
    }

    @Override
    public void glWeightuivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glWeightuivARB(i, ints, i1);
    }

    @Override
    public void glWeightPointerARB(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glWeightPointerARB(i, i1, i2, buffer);
    }

    @Override
    public void glWeightPointerARB(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glWeightPointerARB(i, i1, i2, l);
    }

    @Override
    public void glVertexBlendARB(int i) {
        verifyAccess();
        gl.glVertexBlendARB(i);
    }

    @Override
    public void glVertexAttrib1dARB(int i, double v) {
        verifyAccess();
        gl.glVertexAttrib1dARB(i, v);
    }

    @Override
    public void glVertexAttrib1dvARB(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVertexAttrib1dvARB(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib1dvARB(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glVertexAttrib1dvARB(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib1fARB(int i, float v) {
        verifyAccess();
        gl.glVertexAttrib1fARB(i, v);
    }

    @Override
    public void glVertexAttrib1fvARB(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVertexAttrib1fvARB(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib1fvARB(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glVertexAttrib1fvARB(i, floats, i1);
    }

    @Override
    public void glVertexAttrib1sARB(int i, short i1) {
        verifyAccess();
        gl.glVertexAttrib1sARB(i, i1);
    }

    @Override
    public void glVertexAttrib1svARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib1svARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib1svARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib1svARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib2dARB(int i, double v, double v1) {
        verifyAccess();
        gl.glVertexAttrib2dARB(i, v, v1);
    }

    @Override
    public void glVertexAttrib2dvARB(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVertexAttrib2dvARB(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib2dvARB(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glVertexAttrib2dvARB(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib2fARB(int i, float v, float v1) {
        verifyAccess();
        gl.glVertexAttrib2fARB(i, v, v1);
    }

    @Override
    public void glVertexAttrib2fvARB(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVertexAttrib2fvARB(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib2fvARB(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glVertexAttrib2fvARB(i, floats, i1);
    }

    @Override
    public void glVertexAttrib2sARB(int i, short i1, short i2) {
        verifyAccess();
        gl.glVertexAttrib2sARB(i, i1, i2);
    }

    @Override
    public void glVertexAttrib2svARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib2svARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib2svARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib2svARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib3dARB(int i, double v, double v1, double v2) {
        verifyAccess();
        gl.glVertexAttrib3dARB(i, v, v1, v2);
    }

    @Override
    public void glVertexAttrib3dvARB(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVertexAttrib3dvARB(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib3dvARB(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glVertexAttrib3dvARB(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib3fARB(int i, float v, float v1, float v2) {
        verifyAccess();
        gl.glVertexAttrib3fARB(i, v, v1, v2);
    }

    @Override
    public void glVertexAttrib3fvARB(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVertexAttrib3fvARB(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib3fvARB(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glVertexAttrib3fvARB(i, floats, i1);
    }

    @Override
    public void glVertexAttrib3sARB(int i, short i1, short i2, short i3) {
        verifyAccess();
        gl.glVertexAttrib3sARB(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttrib3svARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib3svARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib3svARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib3svARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4NbvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NbvARB(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4NbvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NbvARB(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4NivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NivARB(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4NivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NivARB(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4NsvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NsvARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4NsvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NsvARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4NubARB(int i, byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        gl.glVertexAttrib4NubARB(i, b, b1, b2, b3);
    }

    @Override
    public void glVertexAttrib4NubvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NubvARB(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4NubvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NubvARB(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4NuivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NuivARB(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4NuivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NuivARB(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4NusvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib4NusvARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4NusvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib4NusvARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4bvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttrib4bvARB(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4bvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttrib4bvARB(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4dARB(int i, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glVertexAttrib4dARB(i, v, v1, v2, v3);
    }

    @Override
    public void glVertexAttrib4dvARB(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVertexAttrib4dvARB(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib4dvARB(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glVertexAttrib4dvARB(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib4fARB(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glVertexAttrib4fARB(i, v, v1, v2, v3);
    }

    @Override
    public void glVertexAttrib4fvARB(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVertexAttrib4fvARB(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib4fvARB(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glVertexAttrib4fvARB(i, floats, i1);
    }

    @Override
    public void glVertexAttrib4ivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttrib4ivARB(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4ivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttrib4ivARB(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4sARB(int i, short i1, short i2, short i3, short i4) {
        verifyAccess();
        gl.glVertexAttrib4sARB(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttrib4svARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib4svARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4svARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib4svARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4ubvARB(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttrib4ubvARB(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4ubvARB(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttrib4ubvARB(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4uivARB(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttrib4uivARB(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4uivARB(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttrib4uivARB(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4usvARB(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttrib4usvARB(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4usvARB(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttrib4usvARB(i, shorts, i1);
    }

    @Override
    public void glVertexAttribPointerARB(int i, int i1, int i2, boolean b, int i3, Buffer buffer) {
        verifyAccess();
        gl.glVertexAttribPointerARB(i, i1, i2, b, i3, buffer);
    }

    @Override
    public void glVertexAttribPointerARB(int i, int i1, int i2, boolean b, int i3, long l) {
        verifyAccess();
        gl.glVertexAttribPointerARB(i, i1, i2, b, i3, l);
    }

    @Override
    public void glEnableVertexAttribArrayARB(int i) {
        verifyAccess();
        gl.glEnableVertexAttribArrayARB(i);
    }

    @Override
    public void glDisableVertexAttribArrayARB(int i) {
        verifyAccess();
        gl.glDisableVertexAttribArrayARB(i);
    }

    @Override
    public void glGetVertexAttribdvARB(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetVertexAttribdvARB(i, i1, doubleBuffer);
    }

    @Override
    public void glGetVertexAttribdvARB(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glGetVertexAttribdvARB(i, i1, doubles, i2);
    }

    @Override
    public void glGetVertexAttribfvARB(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetVertexAttribfvARB(i, i1, floatBuffer);
    }

    @Override
    public void glGetVertexAttribfvARB(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetVertexAttribfvARB(i, i1, floats, i2);
    }

    @Override
    public void glGetVertexAttribivARB(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVertexAttribivARB(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribivARB(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVertexAttribivARB(i, i1, ints, i2);
    }

    @Override
    public void glBlendBarrier() {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendBarrier];
        gl.glBlendBarrier();
    }

    @Override
    public void glMultiTexCoord1bOES(int i, byte b) {
        verifyAccess();
        gl.glMultiTexCoord1bOES(i, b);
    }

    @Override
    public void glMultiTexCoord1bvOES(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glMultiTexCoord1bvOES(i, byteBuffer);
    }

    @Override
    public void glMultiTexCoord1bvOES(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glMultiTexCoord1bvOES(i, bytes, i1);
    }

    @Override
    public void glMultiTexCoord2bOES(int i, byte b, byte b1) {
        verifyAccess();
        gl.glMultiTexCoord2bOES(i, b, b1);
    }

    @Override
    public void glMultiTexCoord2bvOES(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glMultiTexCoord2bvOES(i, byteBuffer);
    }

    @Override
    public void glMultiTexCoord2bvOES(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glMultiTexCoord2bvOES(i, bytes, i1);
    }

    @Override
    public void glMultiTexCoord3bOES(int i, byte b, byte b1, byte b2) {
        verifyAccess();
        gl.glMultiTexCoord3bOES(i, b, b1, b2);
    }

    @Override
    public void glMultiTexCoord3bvOES(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glMultiTexCoord3bvOES(i, byteBuffer);
    }

    @Override
    public void glMultiTexCoord3bvOES(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glMultiTexCoord3bvOES(i, bytes, i1);
    }

    @Override
    public void glMultiTexCoord4bOES(int i, byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        gl.glMultiTexCoord4bOES(i, b, b1, b2, b3);
    }

    @Override
    public void glMultiTexCoord4bvOES(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glMultiTexCoord4bvOES(i, byteBuffer);
    }

    @Override
    public void glMultiTexCoord4bvOES(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glMultiTexCoord4bvOES(i, bytes, i1);
    }

    @Override
    public void glTexCoord1bOES(byte b) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord1bOES(b);
    }

    @Override
    public void glTexCoord1bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord1bvOES(byteBuffer);
    }

    @Override
    public void glTexCoord1bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord1bvOES(bytes, i);
    }

    @Override
    public void glTexCoord2bOES(byte b, byte b1) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord2bOES(b, b1);
    }

    @Override
    public void glTexCoord2bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord2bvOES(byteBuffer);
    }

    @Override
    public void glTexCoord2bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord2bvOES(bytes, i);
    }

    @Override
    public void glTexCoord3bOES(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord3bOES(b, b1, b2);
    }

    @Override
    public void glTexCoord3bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord3bvOES(byteBuffer);
    }

    @Override
    public void glTexCoord3bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord3bvOES(bytes, i);
    }

    @Override
    public void glTexCoord4bOES(byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord4bOES(b, b1, b2, b3);
    }

    @Override
    public void glTexCoord4bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord4bvOES(byteBuffer);
    }

    @Override
    public void glTexCoord4bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoord4bvOES(bytes, i);
    }

    @Override
    public void glVertex2bOES(byte b, byte b1) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex2bOES(b, b1);
    }

    @Override
    public void glVertex2bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex2bvOES(byteBuffer);
    }

    @Override
    public void glVertex2bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex2bvOES(bytes, i);
    }

    @Override
    public void glVertex3bOES(byte b, byte b1, byte b2) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex3bOES(b, b1, b2);
    }

    @Override
    public void glVertex3bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex3bvOES(byteBuffer);
    }

    @Override
    public void glVertex3bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex3bvOES(bytes, i);
    }

    @Override
    public void glVertex4bOES(byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex4bOES(b, b1, b2, b3);
    }

    @Override
    public void glVertex4bvOES(ByteBuffer byteBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex4bvOES(byteBuffer);
    }

    @Override
    public void glVertex4bvOES(byte[] bytes, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glVertex4bvOES(bytes, i);
    }

    @Override
    public int glQueryMatrixxOES(IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        return gl.glQueryMatrixxOES(intBuffer, intBuffer1);
    }

    @Override
    public int glQueryMatrixxOES(int[] ints, int i, int[] ints1, int i1) {
        verifyAccess();
        return gl.glQueryMatrixxOES(ints, i, ints1, i1);
    }

    @Override
    public void glClipPlanef(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClipPlanef];
        gl.glClipPlanef(i, floatBuffer);
    }

    @Override
    public void glClipPlanef(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glClipPlanef];
        gl.glClipPlanef(i, floats, i1);
    }

    @Override
    public void glGetClipPlanef(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetClipPlanef];
        gl.glGetClipPlanef(i, floatBuffer);
    }

    @Override
    public void glGetClipPlanef(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetClipPlanef];
        gl.glGetClipPlanef(i, floats, i1);
    }

    @Override
    public void glBlendFuncIndexedAMD(int i, int i1, int i2) {
        verifyAccess();
        gl.glBlendFuncIndexedAMD(i, i1, i2);
    }

    @Override
    public void glBlendFuncSeparateIndexedAMD(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glBlendFuncSeparateIndexedAMD(i, i1, i2, i3, i4);
    }

    @Override
    public void glBlendEquationIndexedAMD(int i, int i1) {
        verifyAccess();
        gl.glBlendEquationIndexedAMD(i, i1);
    }

    @Override
    public void glBlendEquationSeparateIndexedAMD(int i, int i1, int i2) {
        verifyAccess();
        gl.glBlendEquationSeparateIndexedAMD(i, i1, i2);
    }

    @Override
    public void glUniform1i64NV(int i, long l) {
        verifyAccess();
        gl.glUniform1i64NV(i, l);
    }

    @Override
    public void glUniform2i64NV(int i, long l, long l1) {
        verifyAccess();
        gl.glUniform2i64NV(i, l, l1);
    }

    @Override
    public void glUniform3i64NV(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glUniform3i64NV(i, l, l1, l2);
    }

    @Override
    public void glUniform4i64NV(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glUniform4i64NV(i, l, l1, l2, l3);
    }

    @Override
    public void glUniform1i64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform1i64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform1i64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform1i64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform2i64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform2i64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform2i64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform2i64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform3i64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform3i64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform3i64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform3i64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform4i64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform4i64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform4i64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform4i64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform1ui64NV(int i, long l) {
        verifyAccess();
        gl.glUniform1ui64NV(i, l);
    }

    @Override
    public void glUniform2ui64NV(int i, long l, long l1) {
        verifyAccess();
        gl.glUniform2ui64NV(i, l, l1);
    }

    @Override
    public void glUniform3ui64NV(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glUniform3ui64NV(i, l, l1, l2);
    }

    @Override
    public void glUniform4ui64NV(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glUniform4ui64NV(i, l, l1, l2, l3);
    }

    @Override
    public void glUniform1ui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform1ui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform1ui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform1ui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform2ui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform2ui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform2ui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform2ui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform3ui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform3ui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform3ui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform3ui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glUniform4ui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniform4ui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniform4ui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniform4ui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glGetUniformi64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetUniformi64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetUniformi64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetUniformi64vNV(i, i1, longs, i2);
    }

    @Override
    public void glProgramUniform1i64NV(int i, int i1, long l) {
        verifyAccess();
        gl.glProgramUniform1i64NV(i, i1, l);
    }

    @Override
    public void glProgramUniform2i64NV(int i, int i1, long l, long l1) {
        verifyAccess();
        gl.glProgramUniform2i64NV(i, i1, l, l1);
    }

    @Override
    public void glProgramUniform3i64NV(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        gl.glProgramUniform3i64NV(i, i1, l, l1, l2);
    }

    @Override
    public void glProgramUniform4i64NV(int i, int i1, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glProgramUniform4i64NV(i, i1, l, l1, l2, l3);
    }

    @Override
    public void glProgramUniform1i64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform1i64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform1i64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform1i64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform2i64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform2i64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform2i64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform2i64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform3i64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform3i64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform3i64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform3i64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform4i64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform4i64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform4i64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform4i64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform1ui64NV(int i, int i1, long l) {
        verifyAccess();
        gl.glProgramUniform1ui64NV(i, i1, l);
    }

    @Override
    public void glProgramUniform2ui64NV(int i, int i1, long l, long l1) {
        verifyAccess();
        gl.glProgramUniform2ui64NV(i, i1, l, l1);
    }

    @Override
    public void glProgramUniform3ui64NV(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        gl.glProgramUniform3ui64NV(i, i1, l, l1, l2);
    }

    @Override
    public void glProgramUniform4ui64NV(int i, int i1, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glProgramUniform4ui64NV(i, i1, l, l1, l2, l3);
    }

    @Override
    public void glProgramUniform1ui64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform1ui64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform1ui64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform1ui64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform2ui64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform2ui64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform2ui64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform2ui64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform3ui64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform3ui64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform3ui64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform3ui64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glProgramUniform4ui64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniform4ui64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniform4ui64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniform4ui64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glVertexAttribParameteriAMD(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexAttribParameteriAMD(i, i1, i2);
    }

    @Override
    public void glGenNamesAMD(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGenNamesAMD(i, i1, intBuffer);
    }

    @Override
    public void glGenNamesAMD(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGenNamesAMD(i, i1, ints, i2);
    }

    @Override
    public void glDeleteNamesAMD(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteNamesAMD(i, i1, intBuffer);
    }

    @Override
    public void glDeleteNamesAMD(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glDeleteNamesAMD(i, i1, ints, i2);
    }

    @Override
    public boolean glIsNameAMD(int i, int i1) {
        verifyAccess();
        return gl.glIsNameAMD(i, i1);
    }

    @Override
    public void glQueryObjectParameteruiAMD(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glQueryObjectParameteruiAMD(i, i1, i2, i3);
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(IntBuffer intBuffer, int i, IntBuffer intBuffer1) {
        verifyAccess();
        gl.glGetPerfMonitorGroupsAMD(intBuffer, i, intBuffer1);
    }

    @Override
    public void glGetPerfMonitorGroupsAMD(int[] ints, int i, int i1, int[] ints1, int i2) {
        verifyAccess();
        gl.glGetPerfMonitorGroupsAMD(ints, i, i1, ints1, i2);
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int i, IntBuffer intBuffer, IntBuffer intBuffer1, int i1, IntBuffer intBuffer2) {
        verifyAccess();
        gl.glGetPerfMonitorCountersAMD(i, intBuffer, intBuffer1, i1, intBuffer2);
    }

    @Override
    public void glGetPerfMonitorCountersAMD(int i, int[] ints, int i1, int[] ints1, int i2, int i3, int[] ints2, int i4) {
        verifyAccess();
        gl.glGetPerfMonitorCountersAMD(i, ints, i1, ints1, i2, i3, ints2, i4);
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int i, int i1, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetPerfMonitorGroupStringAMD(i, i1, intBuffer, byteBuffer);
    }

    @Override
    public void glGetPerfMonitorGroupStringAMD(int i, int i1, int[] ints, int i2, byte[] bytes, int i3) {
        verifyAccess();
        gl.glGetPerfMonitorGroupStringAMD(i, i1, ints, i2, bytes, i3);
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int i, int i1, int i2, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetPerfMonitorCounterStringAMD(i, i1, i2, intBuffer, byteBuffer);
    }

    @Override
    public void glGetPerfMonitorCounterStringAMD(int i, int i1, int i2, int[] ints, int i3, byte[] bytes, int i4) {
        verifyAccess();
        gl.glGetPerfMonitorCounterStringAMD(i, i1, i2, ints, i3, bytes, i4);
    }

    @Override
    public void glGetPerfMonitorCounterInfoAMD(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glGetPerfMonitorCounterInfoAMD(i, i1, i2, buffer);
    }

    @Override
    public void glGenPerfMonitorsAMD(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGenPerfMonitorsAMD(i, intBuffer);
    }

    @Override
    public void glGenPerfMonitorsAMD(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGenPerfMonitorsAMD(i, ints, i1);
    }

    @Override
    public void glDeletePerfMonitorsAMD(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeletePerfMonitorsAMD(i, intBuffer);
    }

    @Override
    public void glDeletePerfMonitorsAMD(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeletePerfMonitorsAMD(i, ints, i1);
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int i, boolean b, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glSelectPerfMonitorCountersAMD(i, b, i1, i2, intBuffer);
    }

    @Override
    public void glSelectPerfMonitorCountersAMD(int i, boolean b, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glSelectPerfMonitorCountersAMD(i, b, i1, i2, ints, i3);
    }

    @Override
    public void glBeginPerfMonitorAMD(int i) {
        verifyAccess();
        gl.glBeginPerfMonitorAMD(i);
    }

    @Override
    public void glEndPerfMonitorAMD(int i) {
        verifyAccess();
        gl.glEndPerfMonitorAMD(i);
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int i, int i1, int i2, IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        gl.glGetPerfMonitorCounterDataAMD(i, i1, i2, intBuffer, intBuffer1);
    }

    @Override
    public void glGetPerfMonitorCounterDataAMD(int i, int i1, int i2, int[] ints, int i3, int[] ints1, int i4) {
        verifyAccess();
        gl.glGetPerfMonitorCounterDataAMD(i, i1, i2, ints, i3, ints1, i4);
    }

    @Override
    public void glTexStorageSparseAMD(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glTexStorageSparseAMD(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glTextureStorageSparseAMD(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        gl.glTextureStorageSparseAMD(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glBufferParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBufferParameteri];
        gl.glBufferParameteri(i, i1, i2);
    }

    @Override
    public int glObjectPurgeableAPPLE(int i, int i1, int i2) {
        verifyAccess();
        return gl.glObjectPurgeableAPPLE(i, i1, i2);
    }

    @Override
    public int glObjectUnpurgeableAPPLE(int i, int i1, int i2) {
        verifyAccess();
        return gl.glObjectUnpurgeableAPPLE(i, i1, i2);
    }

    @Override
    public void glGetObjectParameterivAPPLE(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetObjectParameterivAPPLE(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetObjectParameterivAPPLE(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetObjectParameterivAPPLE(i, i1, i2, ints, i3);
    }

    @Override
    public void glTextureRangeAPPLE(int i, int i1, Buffer buffer) {
        verifyAccess();
        gl.glTextureRangeAPPLE(i, i1, buffer);
    }

    @Override
    public void glVertexArrayRangeAPPLE(int i, Buffer buffer) {
        verifyAccess();
        gl.glVertexArrayRangeAPPLE(i, buffer);
    }

    @Override
    public void glFlushVertexArrayRangeAPPLE(int i, Buffer buffer) {
        verifyAccess();
        gl.glFlushVertexArrayRangeAPPLE(i, buffer);
    }

    @Override
    public void glVertexArrayParameteriAPPLE(int i, int i1) {
        verifyAccess();
        gl.glVertexArrayParameteriAPPLE(i, i1);
    }

    @Override
    public void glEnableVertexAttribAPPLE(int i, int i1) {
        verifyAccess();
        gl.glEnableVertexAttribAPPLE(i, i1);
    }

    @Override
    public void glDisableVertexAttribAPPLE(int i, int i1) {
        verifyAccess();
        gl.glDisableVertexAttribAPPLE(i, i1);
    }

    @Override
    public boolean glIsVertexAttribEnabledAPPLE(int i, int i1) {
        verifyAccess();
        return gl.glIsVertexAttribEnabledAPPLE(i, i1);
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int i, int i1, double v, double v1, int i2, int i3, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMapVertexAttrib1dAPPLE(i, i1, v, v1, i2, i3, doubleBuffer);
    }

    @Override
    public void glMapVertexAttrib1dAPPLE(int i, int i1, double v, double v1, int i2, int i3, double[] doubles, int i4) {
        verifyAccess();
        gl.glMapVertexAttrib1dAPPLE(i, i1, v, v1, i2, i3, doubles, i4);
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int i, int i1, float v, float v1, int i2, int i3, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMapVertexAttrib1fAPPLE(i, i1, v, v1, i2, i3, floatBuffer);
    }

    @Override
    public void glMapVertexAttrib1fAPPLE(int i, int i1, float v, float v1, int i2, int i3, float[] floats, int i4) {
        verifyAccess();
        gl.glMapVertexAttrib1fAPPLE(i, i1, v, v1, i2, i3, floats, i4);
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int i, int i1, double v, double v1, int i2, int i3, double v2, double v3, int i4, int i5, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMapVertexAttrib2dAPPLE(i, i1, v, v1, i2, i3, v2, v3, i4, i5, doubleBuffer);
    }

    @Override
    public void glMapVertexAttrib2dAPPLE(int i, int i1, double v, double v1, int i2, int i3, double v2, double v3, int i4, int i5, double[] doubles, int i6) {
        verifyAccess();
        gl.glMapVertexAttrib2dAPPLE(i, i1, v, v1, i2, i3, v2, v3, i4, i5, doubles, i6);
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int i, int i1, float v, float v1, int i2, int i3, float v2, float v3, int i4, int i5, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMapVertexAttrib2fAPPLE(i, i1, v, v1, i2, i3, v2, v3, i4, i5, floatBuffer);
    }

    @Override
    public void glMapVertexAttrib2fAPPLE(int i, int i1, float v, float v1, int i2, int i3, float v2, float v3, int i4, int i5, float[] floats, int i6) {
        verifyAccess();
        gl.glMapVertexAttrib2fAPPLE(i, i1, v, v1, i2, i3, v2, v3, i4, i5, floats, i6);
    }

    @Override
    public void glDrawBuffersATI(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDrawBuffersATI(i, intBuffer);
    }

    @Override
    public void glDrawBuffersATI(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDrawBuffersATI(i, ints, i1);
    }

    @Override
    public void glPNTrianglesiATI(int i, int i1) {
        verifyAccess();
        gl.glPNTrianglesiATI(i, i1);
    }

    @Override
    public void glPNTrianglesfATI(int i, float v) {
        verifyAccess();
        gl.glPNTrianglesfATI(i, v);
    }

    @Override
    public void glUniformBufferEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glUniformBufferEXT(i, i1, i2);
    }

    @Override
    public int glGetUniformBufferSizeEXT(int i, int i1) {
        verifyAccess();
        return gl.glGetUniformBufferSizeEXT(i, i1);
    }

    @Override
    public long glGetUniformOffsetEXT(int i, int i1) {
        verifyAccess();
        return gl.glGetUniformOffsetEXT(i, i1);
    }

    @Override
    public void glLockArraysEXT(int i, int i1) {
        verifyAccess();
        gl.glLockArraysEXT(i, i1);
    }

    @Override
    public void glUnlockArraysEXT() {
        verifyAccess();
        gl.glUnlockArraysEXT();
    }

    @Override
    public void glCullParameterdvEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glCullParameterdvEXT(i, doubleBuffer);
    }

    @Override
    public void glCullParameterdvEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glCullParameterdvEXT(i, doubles, i1);
    }

    @Override
    public void glCullParameterfvEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glCullParameterfvEXT(i, floatBuffer);
    }

    @Override
    public void glCullParameterfvEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glCullParameterfvEXT(i, floats, i1);
    }

    @Override
    public void glDepthBoundsEXT(double v, double v1) {
        verifyAccess();
        gl.glDepthBoundsEXT(v, v1);
    }

    @Override
    public void glMatrixLoadfEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMatrixLoadfEXT(i, floatBuffer);
    }

    @Override
    public void glMatrixLoadfEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glMatrixLoadfEXT(i, floats, i1);
    }

    @Override
    public void glMatrixLoaddEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMatrixLoaddEXT(i, doubleBuffer);
    }

    @Override
    public void glMatrixLoaddEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glMatrixLoaddEXT(i, doubles, i1);
    }

    @Override
    public void glMatrixMultfEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMatrixMultfEXT(i, floatBuffer);
    }

    @Override
    public void glMatrixMultfEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glMatrixMultfEXT(i, floats, i1);
    }

    @Override
    public void glMatrixMultdEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMatrixMultdEXT(i, doubleBuffer);
    }

    @Override
    public void glMatrixMultdEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glMatrixMultdEXT(i, doubles, i1);
    }

    @Override
    public void glMatrixLoadIdentityEXT(int i) {
        verifyAccess();
        gl.glMatrixLoadIdentityEXT(i);
    }

    @Override
    public void glMatrixRotatefEXT(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glMatrixRotatefEXT(i, v, v1, v2, v3);
    }

    @Override
    public void glMatrixRotatedEXT(int i, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glMatrixRotatedEXT(i, v, v1, v2, v3);
    }

    @Override
    public void glMatrixScalefEXT(int i, float v, float v1, float v2) {
        verifyAccess();
        gl.glMatrixScalefEXT(i, v, v1, v2);
    }

    @Override
    public void glMatrixScaledEXT(int i, double v, double v1, double v2) {
        verifyAccess();
        gl.glMatrixScaledEXT(i, v, v1, v2);
    }

    @Override
    public void glMatrixTranslatefEXT(int i, float v, float v1, float v2) {
        verifyAccess();
        gl.glMatrixTranslatefEXT(i, v, v1, v2);
    }

    @Override
    public void glMatrixTranslatedEXT(int i, double v, double v1, double v2) {
        verifyAccess();
        gl.glMatrixTranslatedEXT(i, v, v1, v2);
    }

    @Override
    public void glMatrixFrustumEXT(int i, double v, double v1, double v2, double v3, double v4, double v5) {
        verifyAccess();
        gl.glMatrixFrustumEXT(i, v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glMatrixOrthoEXT(int i, double v, double v1, double v2, double v3, double v4, double v5) {
        verifyAccess();
        gl.glMatrixOrthoEXT(i, v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glMatrixPopEXT(int i) {
        verifyAccess();
        gl.glMatrixPopEXT(i);
    }

    @Override
    public void glMatrixPushEXT(int i) {
        verifyAccess();
        gl.glMatrixPushEXT(i);
    }

    @Override
    public void glClientAttribDefaultEXT(int i) {
        verifyAccess();
        gl.glClientAttribDefaultEXT(i);
    }

    @Override
    public void glPushClientAttribDefaultEXT(int i) {
        verifyAccess();
        gl.glPushClientAttribDefaultEXT(i);
    }

    @Override
    public void glTextureParameterfEXT(int i, int i1, int i2, float v) {
        verifyAccess();
        gl.glTextureParameterfEXT(i, i1, i2, v);
    }

    @Override
    public void glTextureParameterfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glTextureParameterfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glTextureParameterfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glTextureParameterfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glTextureParameteriEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glTextureParameteriEXT(i, i1, i2, i3);
    }

    @Override
    public void glTextureParameterivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glTextureParameterivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glTextureParameterivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glTextureParameterivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glTextureImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        gl.glTextureImage1DEXT(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glTextureImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, long l) {
        verifyAccess();
        gl.glTextureImage1DEXT(i, i1, i2, i3, i4, i5, i6, i7, l);
    }

    @Override
    public void glTextureImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glTextureImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glTextureImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, long l) {
        verifyAccess();
        gl.glTextureImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, l);
    }

    @Override
    public void glTextureSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glTextureSubImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glTextureSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, long l) {
        verifyAccess();
        gl.glTextureSubImage1DEXT(i, i1, i2, i3, i4, i5, i6, l);
    }

    @Override
    public void glTextureSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glTextureSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glTextureSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, long l) {
        verifyAccess();
        gl.glTextureSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, l);
    }

    @Override
    public void glCopyTextureImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        gl.glCopyTextureImage1DEXT(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glCopyTextureImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        verifyAccess();
        gl.glCopyTextureImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8);
    }

    @Override
    public void glCopyTextureSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glCopyTextureSubImage1DEXT(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glCopyTextureSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        verifyAccess();
        gl.glCopyTextureSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8);
    }

    @Override
    public void glGetTextureImageEXT(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        gl.glGetTextureImageEXT(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glGetTextureParameterfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetTextureParameterfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetTextureParameterfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetTextureParameterfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetTextureParameterivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetTextureParameterivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetTextureParameterivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetTextureParameterivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int i, int i1, int i2, int i3, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetTextureLevelParameterfvEXT(i, i1, i2, i3, floatBuffer);
    }

    @Override
    public void glGetTextureLevelParameterfvEXT(int i, int i1, int i2, int i3, float[] floats, int i4) {
        verifyAccess();
        gl.glGetTextureLevelParameterfvEXT(i, i1, i2, i3, floats, i4);
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetTextureLevelParameterivEXT(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glGetTextureLevelParameterivEXT(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glGetTextureLevelParameterivEXT(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glTextureImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        verifyAccess();
        gl.glTextureImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    @Override
    public void glTextureImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, long l) {
        verifyAccess();
        gl.glTextureImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, l);
    }

    @Override
    public void glTextureSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        verifyAccess();
        gl.glTextureSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    @Override
    public void glTextureSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, long l) {
        verifyAccess();
        gl.glTextureSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, l);
    }

    @Override
    public void glCopyTextureSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        verifyAccess();
        gl.glCopyTextureSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9);
    }

    @Override
    public void glBindMultiTextureEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glBindMultiTextureEXT(i, i1, i2);
    }

    @Override
    public void glMultiTexCoordPointerEXT(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexCoordPointerEXT(i, i1, i2, i3, buffer);
    }

    @Override
    public void glMultiTexEnvfEXT(int i, int i1, int i2, float v) {
        verifyAccess();
        gl.glMultiTexEnvfEXT(i, i1, i2, v);
    }

    @Override
    public void glMultiTexEnvfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMultiTexEnvfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glMultiTexEnvfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glMultiTexEnvfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glMultiTexEnviEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glMultiTexEnviEXT(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexEnvivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMultiTexEnvivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glMultiTexEnvivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glMultiTexEnvivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glMultiTexGendEXT(int i, int i1, int i2, double v) {
        verifyAccess();
        gl.glMultiTexGendEXT(i, i1, i2, v);
    }

    @Override
    public void glMultiTexGendvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMultiTexGendvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glMultiTexGendvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glMultiTexGendvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glMultiTexGenfEXT(int i, int i1, int i2, float v) {
        verifyAccess();
        gl.glMultiTexGenfEXT(i, i1, i2, v);
    }

    @Override
    public void glMultiTexGenfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMultiTexGenfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glMultiTexGenfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glMultiTexGenfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glMultiTexGeniEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glMultiTexGeniEXT(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexGenivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMultiTexGenivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glMultiTexGenivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glMultiTexGenivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMultiTexEnvfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetMultiTexEnvfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetMultiTexEnvfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetMultiTexEnvivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexEnvivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMultiTexEnvivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMultiTexEnvivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMultiTexGendvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetMultiTexGendvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glGetMultiTexGendvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glGetMultiTexGendvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glGetMultiTexGenfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMultiTexGenfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetMultiTexGenfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetMultiTexGenfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetMultiTexGenivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexGenivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMultiTexGenivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMultiTexGenivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glMultiTexParameteriEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glMultiTexParameteriEXT(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexParameterivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMultiTexParameterivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glMultiTexParameterivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glMultiTexParameterivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glMultiTexParameterfEXT(int i, int i1, int i2, float v) {
        verifyAccess();
        gl.glMultiTexParameterfEXT(i, i1, i2, v);
    }

    @Override
    public void glMultiTexParameterfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMultiTexParameterfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glMultiTexParameterfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glMultiTexParameterfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glMultiTexImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexImage1DEXT(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glMultiTexImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glMultiTexSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexSubImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glMultiTexSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glCopyMultiTexImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        gl.glCopyMultiTexImage1DEXT(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glCopyMultiTexImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        verifyAccess();
        gl.glCopyMultiTexImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8);
    }

    @Override
    public void glCopyMultiTexSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glCopyMultiTexSubImage1DEXT(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glCopyMultiTexSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        verifyAccess();
        gl.glCopyMultiTexSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8);
    }

    @Override
    public void glGetMultiTexImageEXT(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        gl.glGetMultiTexImageEXT(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMultiTexParameterfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetMultiTexParameterfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetMultiTexParameterfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetMultiTexParameterivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexParameterivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMultiTexParameterivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMultiTexParameterivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int i, int i1, int i2, int i3, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMultiTexLevelParameterfvEXT(i, i1, i2, i3, floatBuffer);
    }

    @Override
    public void glGetMultiTexLevelParameterfvEXT(int i, int i1, int i2, int i3, float[] floats, int i4) {
        verifyAccess();
        gl.glGetMultiTexLevelParameterfvEXT(i, i1, i2, i3, floats, i4);
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexLevelParameterivEXT(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glGetMultiTexLevelParameterivEXT(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glGetMultiTexLevelParameterivEXT(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glMultiTexImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    @Override
    public void glMultiTexSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        verifyAccess();
        gl.glMultiTexSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    @Override
    public void glCopyMultiTexSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        verifyAccess();
        gl.glCopyMultiTexSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9);
    }

    @Override
    public void glEnableClientStateIndexedEXT(int i, int i1) {
        verifyAccess();
        gl.glEnableClientStateIndexedEXT(i, i1);
    }

    @Override
    public void glDisableClientStateIndexedEXT(int i, int i1) {
        verifyAccess();
        gl.glDisableClientStateIndexedEXT(i, i1);
    }

    @Override
    public void glGetFloatIndexedvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetFloatIndexedvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetFloatIndexedvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetFloatIndexedvEXT(i, i1, floats, i2);
    }

    @Override
    public void glGetDoubleIndexedvEXT(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetDoubleIndexedvEXT(i, i1, doubleBuffer);
    }

    @Override
    public void glGetDoubleIndexedvEXT(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glGetDoubleIndexedvEXT(i, i1, doubles, i2);
    }

    @Override
    public void glEnableIndexed(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEnableIndexed];
        gl.glEnableIndexed(i, i1);
    }

    @Override
    public void glDisableIndexed(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDisableIndexed];
        gl.glDisableIndexed(i, i1);
    }

    @Override
    public boolean glIsEnabledIndexed(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsEnabledIndexed];
        return gl.glIsEnabledIndexed(i, i1);
    }

    @Override
    public void glGetIntegerIndexedv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegerIndexedv];
        gl.glGetIntegerIndexedv(i, i1, intBuffer);
    }

    @Override
    public void glGetIntegerIndexedv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegerIndexedv];
        gl.glGetIntegerIndexedv(i, i1, ints, i2);
    }

    @Override
    public void glGetBooleanIndexedv(int i, int i1, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleanIndexedv];
        gl.glGetBooleanIndexedv(i, i1, byteBuffer);
    }

    @Override
    public void glGetBooleanIndexedv(int i, int i1, byte[] bytes, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleanIndexedv];
        gl.glGetBooleanIndexedv(i, i1, bytes, i2);
    }

    @Override
    public void glCompressedTextureImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glCompressedTextureImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glCompressedTextureImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glCompressedTextureSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    @Override
    public void glCompressedTextureSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glCompressedTextureSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glCompressedTextureSubImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glGetCompressedTextureImageEXT(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glGetCompressedTextureImageEXT(i, i1, i2, buffer);
    }

    @Override
    public void glCompressedMultiTexImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glCompressedMultiTexImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glCompressedMultiTexImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glCompressedMultiTexSubImage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexSubImage3DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, buffer);
    }

    @Override
    public void glCompressedMultiTexSubImage2DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexSubImage2DEXT(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glCompressedMultiTexSubImage1DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        gl.glCompressedMultiTexSubImage1DEXT(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glGetCompressedMultiTexImageEXT(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glGetCompressedMultiTexImageEXT(i, i1, i2, buffer);
    }

    @Override
    public void glMatrixLoadTransposefEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMatrixLoadTransposefEXT(i, floatBuffer);
    }

    @Override
    public void glMatrixLoadTransposefEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glMatrixLoadTransposefEXT(i, floats, i1);
    }

    @Override
    public void glMatrixLoadTransposedEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMatrixLoadTransposedEXT(i, doubleBuffer);
    }

    @Override
    public void glMatrixLoadTransposedEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glMatrixLoadTransposedEXT(i, doubles, i1);
    }

    @Override
    public void glMatrixMultTransposefEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMatrixMultTransposefEXT(i, floatBuffer);
    }

    @Override
    public void glMatrixMultTransposefEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glMatrixMultTransposefEXT(i, floats, i1);
    }

    @Override
    public void glMatrixMultTransposedEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glMatrixMultTransposedEXT(i, doubleBuffer);
    }

    @Override
    public void glMatrixMultTransposedEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glMatrixMultTransposedEXT(i, doubles, i1);
    }

    @Override
    public void glNamedBufferDataEXT(int i, long l, Buffer buffer, int i1) {
        verifyAccess();
        gl.glNamedBufferDataEXT(i, l, buffer, i1);
    }

    @Override
    public void glNamedBufferSubDataEXT(int i, long l, long l1, Buffer buffer) {
        verifyAccess();
        gl.glNamedBufferSubDataEXT(i, l, l1, buffer);
    }

    @Override
    public ByteBuffer glMapNamedBufferEXT(int i, int i1) {
        return gl.glMapNamedBufferEXT(i, i1);
    }

    @Override
    public boolean glUnmapNamedBufferEXT(int i) {
        verifyAccess();
        return gl.glUnmapNamedBufferEXT(i);
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedBufferParameterivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetNamedBufferParameterivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetNamedBufferParameterivEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetNamedBufferSubDataEXT(int i, long l, long l1, Buffer buffer) {
        verifyAccess();
        gl.glGetNamedBufferSubDataEXT(i, l, l1, buffer);
    }

    @Override
    public void glTextureBufferEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glTextureBufferEXT(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexBufferEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glMultiTexBufferEXT(i, i1, i2, i3);
    }

    @Override
    public void glTextureParameterIivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glTextureParameterIivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glTextureParameterIivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glTextureParameterIivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glTextureParameterIuivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glTextureParameterIuivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glTextureParameterIuivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glTextureParameterIuivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetTextureParameterIivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetTextureParameterIivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetTextureParameterIivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetTextureParameterIivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetTextureParameterIuivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetTextureParameterIuivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetTextureParameterIuivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetTextureParameterIuivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glMultiTexParameterIivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMultiTexParameterIivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glMultiTexParameterIivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glMultiTexParameterIivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glMultiTexParameterIuivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMultiTexParameterIuivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glMultiTexParameterIuivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glMultiTexParameterIuivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexParameterIivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMultiTexParameterIivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMultiTexParameterIivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMultiTexParameterIuivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMultiTexParameterIuivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMultiTexParameterIuivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int i, int i1, int i2, int i3, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParameters4fvEXT(i, i1, i2, i3, floatBuffer);
    }

    @Override
    public void glNamedProgramLocalParameters4fvEXT(int i, int i1, int i2, int i3, float[] floats, int i4) {
        verifyAccess();
        gl.glNamedProgramLocalParameters4fvEXT(i, i1, i2, i3, floats, i4);
    }

    @Override
    public void glNamedProgramLocalParameterI4iEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4iEXT(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4ivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glNamedProgramLocalParameterI4ivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4ivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParametersI4ivEXT(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glNamedProgramLocalParametersI4ivEXT(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glNamedProgramLocalParametersI4ivEXT(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glNamedProgramLocalParameterI4uiEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4uiEXT(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4uivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glNamedProgramLocalParameterI4uivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glNamedProgramLocalParameterI4uivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParametersI4uivEXT(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glNamedProgramLocalParametersI4uivEXT(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glNamedProgramLocalParametersI4uivEXT(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterIivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetNamedProgramLocalParameterIivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterIivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterIuivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetNamedProgramLocalParameterIuivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterIuivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glEnableClientStateiEXT(int i, int i1) {
        verifyAccess();
        gl.glEnableClientStateiEXT(i, i1);
    }

    @Override
    public void glDisableClientStateiEXT(int i, int i1) {
        verifyAccess();
        gl.glDisableClientStateiEXT(i, i1);
    }

    @Override
    public void glGetFloati_vEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetFloati_vEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetFloati_vEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetFloati_vEXT(i, i1, floats, i2);
    }

    @Override
    public void glGetDoublei_vEXT(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetDoublei_vEXT(i, i1, doubleBuffer);
    }

    @Override
    public void glGetDoublei_vEXT(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        gl.glGetDoublei_vEXT(i, i1, doubles, i2);
    }

    @Override
    public void glGetPointeri_vEXT(int i, int i1, PointerBuffer pointerBuffer) {
        verifyAccess();
        gl.glGetPointeri_vEXT(i, i1, pointerBuffer);
    }

    @Override
    public void glNamedProgramStringEXT(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        gl.glNamedProgramStringEXT(i, i1, i2, i3, buffer);
    }

    @Override
    public void glNamedProgramLocalParameter4dEXT(int i, int i1, int i2, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4dEXT(i, i1, i2, v, v1, v2, v3);
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4dvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glNamedProgramLocalParameter4dvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4dvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glNamedProgramLocalParameter4fEXT(int i, int i1, int i2, float v, float v1, float v2, float v3) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4fEXT(i, i1, i2, v, v1, v2, v3);
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4fvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glNamedProgramLocalParameter4fvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glNamedProgramLocalParameter4fvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterdvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glGetNamedProgramLocalParameterdvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterdvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterfvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetNamedProgramLocalParameterfvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetNamedProgramLocalParameterfvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetNamedProgramivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedProgramivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetNamedProgramivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetNamedProgramivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetNamedProgramStringEXT(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glGetNamedProgramStringEXT(i, i1, i2, buffer);
    }

    @Override
    public void glNamedRenderbufferStorageEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glNamedRenderbufferStorageEXT(i, i1, i2, i3);
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedRenderbufferParameterivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetNamedRenderbufferParameterivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetNamedRenderbufferParameterivEXT(i, i1, ints, i2);
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glNamedRenderbufferStorageMultisampleEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glNamedRenderbufferStorageMultisampleCoverageEXT(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glNamedRenderbufferStorageMultisampleCoverageEXT(i, i1, i2, i3, i4, i5);
    }

    @Override
    public int glCheckNamedFramebufferStatusEXT(int i, int i1) {
        verifyAccess();
        return gl.glCheckNamedFramebufferStatusEXT(i, i1);
    }

    @Override
    public void glNamedFramebufferTexture1DEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glNamedFramebufferTexture1DEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glNamedFramebufferTexture2DEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glNamedFramebufferTexture2DEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glNamedFramebufferTexture3DEXT(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glNamedFramebufferTexture3DEXT(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glNamedFramebufferRenderbufferEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glNamedFramebufferRenderbufferEXT(i, i1, i2, i3);
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNamedFramebufferAttachmentParameterivEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetNamedFramebufferAttachmentParameterivEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetNamedFramebufferAttachmentParameterivEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGenerateTextureMipmapEXT(int i, int i1) {
        verifyAccess();
        gl.glGenerateTextureMipmapEXT(i, i1);
    }

    @Override
    public void glGenerateMultiTexMipmapEXT(int i, int i1) {
        verifyAccess();
        gl.glGenerateMultiTexMipmapEXT(i, i1);
    }

    @Override
    public void glFramebufferDrawBufferEXT(int i, int i1) {
        verifyAccess();
        gl.glFramebufferDrawBufferEXT(i, i1);
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glFramebufferDrawBuffersEXT(i, i1, intBuffer);
    }

    @Override
    public void glFramebufferDrawBuffersEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glFramebufferDrawBuffersEXT(i, i1, ints, i2);
    }

    @Override
    public void glFramebufferReadBufferEXT(int i, int i1) {
        verifyAccess();
        gl.glFramebufferReadBufferEXT(i, i1);
    }

    @Override
    public void glGetFramebufferParameterivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetFramebufferParameterivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetFramebufferParameterivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetFramebufferParameterivEXT(i, i1, ints, i2);
    }

    @Override
    public void glNamedCopyBufferSubDataEXT(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        gl.glNamedCopyBufferSubDataEXT(i, i1, l, l1, l2);
    }

    @Override
    public void glNamedFramebufferTextureEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glNamedFramebufferTextureEXT(i, i1, i2, i3);
    }

    @Override
    public void glNamedFramebufferTextureLayerEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glNamedFramebufferTextureLayerEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glNamedFramebufferTextureFaceEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glNamedFramebufferTextureFaceEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glTextureRenderbufferEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glTextureRenderbufferEXT(i, i1, i2);
    }

    @Override
    public void glMultiTexRenderbufferEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glMultiTexRenderbufferEXT(i, i1, i2);
    }

    @Override
    public void glVertexArrayVertexOffsetEXT(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        gl.glVertexArrayVertexOffsetEXT(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glVertexArrayColorOffsetEXT(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        gl.glVertexArrayColorOffsetEXT(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glVertexArrayEdgeFlagOffsetEXT(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glVertexArrayEdgeFlagOffsetEXT(i, i1, i2, l);
    }

    @Override
    public void glVertexArrayIndexOffsetEXT(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        gl.glVertexArrayIndexOffsetEXT(i, i1, i2, i3, l);
    }

    @Override
    public void glVertexArrayNormalOffsetEXT(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        gl.glVertexArrayNormalOffsetEXT(i, i1, i2, i3, l);
    }

    @Override
    public void glVertexArrayTexCoordOffsetEXT(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        gl.glVertexArrayTexCoordOffsetEXT(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glVertexArrayMultiTexCoordOffsetEXT(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        gl.glVertexArrayMultiTexCoordOffsetEXT(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glVertexArrayFogCoordOffsetEXT(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        gl.glVertexArrayFogCoordOffsetEXT(i, i1, i2, i3, l);
    }

    @Override
    public void glVertexArraySecondaryColorOffsetEXT(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        gl.glVertexArraySecondaryColorOffsetEXT(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glVertexArrayVertexAttribOffsetEXT(int i, int i1, int i2, int i3, int i4, boolean b, int i5, long l) {
        verifyAccess();
        gl.glVertexArrayVertexAttribOffsetEXT(i, i1, i2, i3, i4, b, i5, l);
    }

    @Override
    public void glVertexArrayVertexAttribIOffsetEXT(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        gl.glVertexArrayVertexAttribIOffsetEXT(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glEnableVertexArrayEXT(int i, int i1) {
        verifyAccess();
        gl.glEnableVertexArrayEXT(i, i1);
    }

    @Override
    public void glDisableVertexArrayEXT(int i, int i1) {
        verifyAccess();
        gl.glDisableVertexArrayEXT(i, i1);
    }

    @Override
    public void glEnableVertexArrayAttribEXT(int i, int i1) {
        verifyAccess();
        gl.glEnableVertexArrayAttribEXT(i, i1);
    }

    @Override
    public void glDisableVertexArrayAttribEXT(int i, int i1) {
        verifyAccess();
        gl.glDisableVertexArrayAttribEXT(i, i1);
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVertexArrayIntegervEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexArrayIntegervEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVertexArrayIntegervEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetVertexArrayPointervEXT(int i, int i1, PointerBuffer pointerBuffer) {
        verifyAccess();
        gl.glGetVertexArrayPointervEXT(i, i1, pointerBuffer);
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVertexArrayIntegeri_vEXT(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetVertexArrayIntegeri_vEXT(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetVertexArrayIntegeri_vEXT(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetVertexArrayPointeri_vEXT(int i, int i1, int i2, PointerBuffer pointerBuffer) {
        verifyAccess();
        gl.glGetVertexArrayPointeri_vEXT(i, i1, i2, pointerBuffer);
    }

    @Override
    public ByteBuffer glMapNamedBufferRangeEXT(int i, long l, long l1, int i1) {
        return gl.glMapNamedBufferRangeEXT(i, l, l1, i1);
    }

    @Override
    public void glFlushMappedNamedBufferRangeEXT(int i, long l, long l1) {
        verifyAccess();
        gl.glFlushMappedNamedBufferRangeEXT(i, l, l1);
    }

    @Override
    public void glNamedBufferStorageEXT(int i, long l, Buffer buffer, int i1) {
        verifyAccess();
        gl.glNamedBufferStorageEXT(i, l, buffer, i1);
    }

    @Override
    public void glProgramUniform1dEXT(int i, int i1, double v) {
        verifyAccess();
        gl.glProgramUniform1dEXT(i, i1, v);
    }

    @Override
    public void glProgramUniform2dEXT(int i, int i1, double v, double v1) {
        verifyAccess();
        gl.glProgramUniform2dEXT(i, i1, v, v1);
    }

    @Override
    public void glProgramUniform3dEXT(int i, int i1, double v, double v1, double v2) {
        verifyAccess();
        gl.glProgramUniform3dEXT(i, i1, v, v1, v2);
    }

    @Override
    public void glProgramUniform4dEXT(int i, int i1, double v, double v1, double v2, double v3) {
        verifyAccess();
        gl.glProgramUniform4dEXT(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramUniform1dvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniform1dvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform1dvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniform1dvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform2dvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniform2dvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform2dvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniform2dvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform3dvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniform3dvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform3dvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniform3dvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform4dvEXT(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniform4dvEXT(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform4dvEXT(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniform4dvEXT(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix2dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix2dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix3dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix3dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix4dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix4dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2x3dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix2x3dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x3dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix2x3dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2x4dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix2x4dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x4dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix2x4dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3x2dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix3x2dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x2dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix3x2dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3x4dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix3x4dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x4dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix3x4dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4x2dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix4x2dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x2dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix4x2dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4x3dvEXT(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glProgramUniformMatrix4x3dvEXT(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x3dvEXT(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        gl.glProgramUniformMatrix4x3dvEXT(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glTextureBufferRangeEXT(int i, int i1, int i2, int i3, long l, long l1) {
        verifyAccess();
        gl.glTextureBufferRangeEXT(i, i1, i2, i3, l, l1);
    }

    @Override
    public void glTextureStorage2DMultisampleEXT(int i, int i1, int i2, int i3, int i4, int i5, boolean b) {
        verifyAccess();
        gl.glTextureStorage2DMultisampleEXT(i, i1, i2, i3, i4, i5, b);
    }

    @Override
    public void glTextureStorage3DMultisampleEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b) {
        verifyAccess();
        gl.glTextureStorage3DMultisampleEXT(i, i1, i2, i3, i4, i5, i6, b);
    }

    @Override
    public void glVertexArrayBindVertexBufferEXT(int i, int i1, int i2, long l, int i3) {
        verifyAccess();
        gl.glVertexArrayBindVertexBufferEXT(i, i1, i2, l, i3);
    }

    @Override
    public void glVertexArrayVertexAttribFormatEXT(int i, int i1, int i2, int i3, boolean b, int i4) {
        verifyAccess();
        gl.glVertexArrayVertexAttribFormatEXT(i, i1, i2, i3, b, i4);
    }

    @Override
    public void glVertexArrayVertexAttribIFormatEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glVertexArrayVertexAttribIFormatEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexArrayVertexAttribLFormatEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glVertexArrayVertexAttribLFormatEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexArrayVertexAttribBindingEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexArrayVertexAttribBindingEXT(i, i1, i2);
    }

    @Override
    public void glVertexArrayVertexBindingDivisorEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexArrayVertexBindingDivisorEXT(i, i1, i2);
    }

    @Override
    public void glVertexArrayVertexAttribLOffsetEXT(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        gl.glVertexArrayVertexAttribLOffsetEXT(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glTexturePageCommitmentEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, boolean b) {
        verifyAccess();
        gl.glTexturePageCommitmentEXT(i, i1, i2, i3, i4, i5, i6, i7, b);
    }

    @Override
    public void glVertexArrayVertexAttribDivisorEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexArrayVertexAttribDivisorEXT(i, i1, i2);
    }

    @Override
    public void glColorMaskIndexed(int i, boolean b, boolean b1, boolean b2, boolean b3) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorMaskIndexed];
        gl.glColorMaskIndexed(i, b, b1, b2, b3);
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glProgramEnvParameters4fvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramEnvParameters4fvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glProgramEnvParameters4fvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glProgramLocalParameters4fvEXT(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramLocalParameters4fvEXT(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glProgramLocalParameters4fvEXT(i, i1, i2, floats, i3);
    }

    @Override
    public void glIndexFuncEXT(int i, float v) {
        verifyAccess();
        gl.glIndexFuncEXT(i, v);
    }

    @Override
    public void glIndexMaterialEXT(int i, int i1) {
        verifyAccess();
        gl.glIndexMaterialEXT(i, i1);
    }

    @Override
    public void glApplyTextureEXT(int i) {
        verifyAccess();
        gl.glApplyTextureEXT(i);
    }

    @Override
    public void glTextureLightEXT(int i) {
        verifyAccess();
        gl.glTextureLightEXT(i);
    }

    @Override
    public void glTextureMaterialEXT(int i, int i1) {
        verifyAccess();
        gl.glTextureMaterialEXT(i, i1);
    }

    @Override
    public void glPixelTransformParameteriEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glPixelTransformParameteriEXT(i, i1, i2);
    }

    @Override
    public void glPixelTransformParameterfEXT(int i, int i1, float v) {
        verifyAccess();
        gl.glPixelTransformParameterfEXT(i, i1, v);
    }

    @Override
    public void glPixelTransformParameterivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glPixelTransformParameterivEXT(i, i1, intBuffer);
    }

    @Override
    public void glPixelTransformParameterivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glPixelTransformParameterivEXT(i, i1, ints, i2);
    }

    @Override
    public void glPixelTransformParameterfvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glPixelTransformParameterfvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glPixelTransformParameterfvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glPixelTransformParameterfvEXT(i, i1, floats, i2);
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetPixelTransformParameterivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetPixelTransformParameterivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetPixelTransformParameterivEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetPixelTransformParameterfvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetPixelTransformParameterfvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetPixelTransformParameterfvEXT(i, i1, floats, i2);
    }

    @Override
    public void glPolygonOffsetClampEXT(float v, float v1, float v2) {
        verifyAccess();
        gl.glPolygonOffsetClampEXT(v, v1, v2);
    }

    @Override
    public void glProvokingVertexEXT(int i) {
        verifyAccess();
        gl.glProvokingVertexEXT(i);
    }

    @Override
    public void glRasterSamplesEXT(int i, boolean b) {
        verifyAccess();
        gl.glRasterSamplesEXT(i, b);
    }

    @Override
    public void glStencilClearTagEXT(int i, int i1) {
        verifyAccess();
        gl.glStencilClearTagEXT(i, i1);
    }

    @Override
    public void glActiveStencilFaceEXT(int i) {
        verifyAccess();
        gl.glActiveStencilFaceEXT(i);
    }

    @Override
    public void glClearColorIi(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearColorIi];
        gl.glClearColorIi(i, i1, i2, i3);
    }

    @Override
    public void glClearColorIui(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearColorIui];
        gl.glClearColorIui(i, i1, i2, i3);
    }

    @Override
    public void glTextureNormalEXT(int i) {
        verifyAccess();
        gl.glTextureNormalEXT(i);
    }

    @Override
    public void glGetQueryObjecti64vEXT(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetQueryObjecti64vEXT(i, i1, longBuffer);
    }

    @Override
    public void glGetQueryObjecti64vEXT(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetQueryObjecti64vEXT(i, i1, longs, i2);
    }

    @Override
    public void glGetQueryObjectui64vEXT(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetQueryObjectui64vEXT(i, i1, longBuffer);
    }

    @Override
    public void glGetQueryObjectui64vEXT(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetQueryObjectui64vEXT(i, i1, longs, i2);
    }

    @Override
    public void glBeginVertexShaderEXT() {
        verifyAccess();
        gl.glBeginVertexShaderEXT();
    }

    @Override
    public void glEndVertexShaderEXT() {
        verifyAccess();
        gl.glEndVertexShaderEXT();
    }

    @Override
    public void glBindVertexShaderEXT(int i) {
        verifyAccess();
        gl.glBindVertexShaderEXT(i);
    }

    @Override
    public int glGenVertexShadersEXT(int i) {
        verifyAccess();
        return gl.glGenVertexShadersEXT(i);
    }

    @Override
    public void glDeleteVertexShaderEXT(int i) {
        verifyAccess();
        gl.glDeleteVertexShaderEXT(i);
    }

    @Override
    public void glShaderOp1EXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glShaderOp1EXT(i, i1, i2);
    }

    @Override
    public void glShaderOp2EXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glShaderOp2EXT(i, i1, i2, i3);
    }

    @Override
    public void glShaderOp3EXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glShaderOp3EXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glSwizzleEXT(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glSwizzleEXT(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glWriteMaskEXT(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glWriteMaskEXT(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glInsertComponentEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glInsertComponentEXT(i, i1, i2);
    }

    @Override
    public void glExtractComponentEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glExtractComponentEXT(i, i1, i2);
    }

    @Override
    public int glGenSymbolsEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        return gl.glGenSymbolsEXT(i, i1, i2, i3);
    }

    @Override
    public void glSetInvariantEXT(int i, int i1, Buffer buffer) {
        verifyAccess();
        gl.glSetInvariantEXT(i, i1, buffer);
    }

    @Override
    public void glSetLocalConstantEXT(int i, int i1, Buffer buffer) {
        verifyAccess();
        gl.glSetLocalConstantEXT(i, i1, buffer);
    }

    @Override
    public void glVariantbvEXT(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVariantbvEXT(i, byteBuffer);
    }

    @Override
    public void glVariantbvEXT(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVariantbvEXT(i, bytes, i1);
    }

    @Override
    public void glVariantsvEXT(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVariantsvEXT(i, shortBuffer);
    }

    @Override
    public void glVariantsvEXT(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVariantsvEXT(i, shorts, i1);
    }

    @Override
    public void glVariantivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVariantivEXT(i, intBuffer);
    }

    @Override
    public void glVariantivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVariantivEXT(i, ints, i1);
    }

    @Override
    public void glVariantfvEXT(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVariantfvEXT(i, floatBuffer);
    }

    @Override
    public void glVariantfvEXT(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glVariantfvEXT(i, floats, i1);
    }

    @Override
    public void glVariantdvEXT(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVariantdvEXT(i, doubleBuffer);
    }

    @Override
    public void glVariantdvEXT(int i, double[] doubles, int i1) {
        verifyAccess();
        gl.glVariantdvEXT(i, doubles, i1);
    }

    @Override
    public void glVariantubvEXT(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVariantubvEXT(i, byteBuffer);
    }

    @Override
    public void glVariantubvEXT(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVariantubvEXT(i, bytes, i1);
    }

    @Override
    public void glVariantusvEXT(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVariantusvEXT(i, shortBuffer);
    }

    @Override
    public void glVariantusvEXT(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVariantusvEXT(i, shorts, i1);
    }

    @Override
    public void glVariantuivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVariantuivEXT(i, intBuffer);
    }

    @Override
    public void glVariantuivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVariantuivEXT(i, ints, i1);
    }

    @Override
    public void glVariantPointerEXT(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glVariantPointerEXT(i, i1, i2, buffer);
    }

    @Override
    public void glVariantPointerEXT(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glVariantPointerEXT(i, i1, i2, l);
    }

    @Override
    public void glEnableVariantClientStateEXT(int i) {
        verifyAccess();
        gl.glEnableVariantClientStateEXT(i);
    }

    @Override
    public void glDisableVariantClientStateEXT(int i) {
        verifyAccess();
        gl.glDisableVariantClientStateEXT(i);
    }

    @Override
    public int glBindLightParameterEXT(int i, int i1) {
        verifyAccess();
        return gl.glBindLightParameterEXT(i, i1);
    }

    @Override
    public int glBindMaterialParameterEXT(int i, int i1) {
        verifyAccess();
        return gl.glBindMaterialParameterEXT(i, i1);
    }

    @Override
    public int glBindTexGenParameterEXT(int i, int i1, int i2) {
        verifyAccess();
        return gl.glBindTexGenParameterEXT(i, i1, i2);
    }

    @Override
    public int glBindTextureUnitParameterEXT(int i, int i1) {
        verifyAccess();
        return gl.glBindTextureUnitParameterEXT(i, i1);
    }

    @Override
    public int glBindParameterEXT(int i) {
        verifyAccess();
        return gl.glBindParameterEXT(i);
    }

    @Override
    public boolean glIsVariantEnabledEXT(int i, int i1) {
        verifyAccess();
        return gl.glIsVariantEnabledEXT(i, i1);
    }

    @Override
    public void glGetVariantBooleanvEXT(int i, int i1, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetVariantBooleanvEXT(i, i1, byteBuffer);
    }

    @Override
    public void glGetVariantBooleanvEXT(int i, int i1, byte[] bytes, int i2) {
        verifyAccess();
        gl.glGetVariantBooleanvEXT(i, i1, bytes, i2);
    }

    @Override
    public void glGetVariantIntegervEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVariantIntegervEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetVariantIntegervEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVariantIntegervEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetVariantFloatvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetVariantFloatvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetVariantFloatvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetVariantFloatvEXT(i, i1, floats, i2);
    }

    @Override
    public void glGetInvariantBooleanvEXT(int i, int i1, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetInvariantBooleanvEXT(i, i1, byteBuffer);
    }

    @Override
    public void glGetInvariantBooleanvEXT(int i, int i1, byte[] bytes, int i2) {
        verifyAccess();
        gl.glGetInvariantBooleanvEXT(i, i1, bytes, i2);
    }

    @Override
    public void glGetInvariantIntegervEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetInvariantIntegervEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetInvariantIntegervEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetInvariantIntegervEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetInvariantFloatvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetInvariantFloatvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetInvariantFloatvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetInvariantFloatvEXT(i, i1, floats, i2);
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int i, int i1, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glGetLocalConstantBooleanvEXT(i, i1, byteBuffer);
    }

    @Override
    public void glGetLocalConstantBooleanvEXT(int i, int i1, byte[] bytes, int i2) {
        verifyAccess();
        gl.glGetLocalConstantBooleanvEXT(i, i1, bytes, i2);
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetLocalConstantIntegervEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetLocalConstantIntegervEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetLocalConstantIntegervEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetLocalConstantFloatvEXT(i, i1, floatBuffer);
    }

    @Override
    public void glGetLocalConstantFloatvEXT(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetLocalConstantFloatvEXT(i, i1, floats, i2);
    }

    @Override
    public void glVertexWeightfEXT(float v) {
        verifyAccess();
        gl.glVertexWeightfEXT(v);
    }

    @Override
    public void glVertexWeightfvEXT(FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVertexWeightfvEXT(floatBuffer);
    }

    @Override
    public void glVertexWeightfvEXT(float[] floats, int i) {
        verifyAccess();
        gl.glVertexWeightfvEXT(floats, i);
    }

    @Override
    public void glVertexWeightPointerEXT(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        gl.glVertexWeightPointerEXT(i, i1, i2, buffer);
    }

    @Override
    public void glVertexWeightPointerEXT(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glVertexWeightPointerEXT(i, i1, i2, l);
    }

    @Override
    public void glFrameTerminatorGREMEDY() {
        verifyAccess();
        gl.glFrameTerminatorGREMEDY();
    }

    @Override
    public void glStringMarkerGREMEDY(int i, Buffer buffer) {
        verifyAccess();
        gl.glStringMarkerGREMEDY(i, buffer);
    }

    @Override
    public void glBlendFuncSeparateINGR(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glBlendFuncSeparateINGR(i, i1, i2, i3);
    }

    @Override
    public void glSyncTextureINTEL(int i) {
        verifyAccess();
        gl.glSyncTextureINTEL(i);
    }

    @Override
    public void glUnmapTexture2DINTEL(int i, int i1) {
        verifyAccess();
        gl.glUnmapTexture2DINTEL(i, i1);
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int i, int i1, int i2, IntBuffer intBuffer, IntBuffer intBuffer1) {
        return gl.glMapTexture2DINTEL(i, i1, i2, intBuffer, intBuffer1);
    }

    @Override
    public ByteBuffer glMapTexture2DINTEL(int i, int i1, int i2, int[] ints, int i3, int[] ints1, int i4) {
        return gl.glMapTexture2DINTEL(i, i1, i2, ints, i3, ints1, i4);
    }

    @Override
    public void glBeginPerfQueryINTEL(int i) {
        verifyAccess();
        gl.glBeginPerfQueryINTEL(i);
    }

    @Override
    public void glCreatePerfQueryINTEL(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glCreatePerfQueryINTEL(i, intBuffer);
    }

    @Override
    public void glCreatePerfQueryINTEL(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glCreatePerfQueryINTEL(i, ints, i1);
    }

    @Override
    public void glDeletePerfQueryINTEL(int i) {
        verifyAccess();
        gl.glDeletePerfQueryINTEL(i);
    }

    @Override
    public void glEndPerfQueryINTEL(int i) {
        verifyAccess();
        gl.glEndPerfQueryINTEL(i);
    }

    @Override
    public void glGetFirstPerfQueryIdINTEL(IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetFirstPerfQueryIdINTEL(intBuffer);
    }

    @Override
    public void glGetFirstPerfQueryIdINTEL(int[] ints, int i) {
        verifyAccess();
        gl.glGetFirstPerfQueryIdINTEL(ints, i);
    }

    @Override
    public void glGetNextPerfQueryIdINTEL(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetNextPerfQueryIdINTEL(i, intBuffer);
    }

    @Override
    public void glGetNextPerfQueryIdINTEL(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGetNextPerfQueryIdINTEL(i, ints, i1);
    }

    @Override
    public void glGetPerfCounterInfoINTEL(int i, int i1, int i2, ByteBuffer byteBuffer, int i3, ByteBuffer byteBuffer1, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, IntBuffer intBuffer3, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetPerfCounterInfoINTEL(i, i1, i2, byteBuffer, i3, byteBuffer1, intBuffer, intBuffer1, intBuffer2, intBuffer3, longBuffer);
    }

    @Override
    public void glGetPerfCounterInfoINTEL(int i, int i1, int i2, byte[] bytes, int i3, int i4, byte[] bytes1, int i5, int[] ints, int i6, int[] ints1, int i7, int[] ints2, int i8, int[] ints3, int i9, long[] longs, int i10) {
        verifyAccess();
        gl.glGetPerfCounterInfoINTEL(i, i1, i2, bytes, i3, i4, bytes1, i5, ints, i6, ints1, i7, ints2, i8, ints3, i9, longs, i10);
    }

    @Override
    public void glGetPerfQueryDataINTEL(int i, int i1, int i2, Buffer buffer, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetPerfQueryDataINTEL(i, i1, i2, buffer, intBuffer);
    }

    @Override
    public void glGetPerfQueryDataINTEL(int i, int i1, int i2, Buffer buffer, int[] ints, int i3) {
        verifyAccess();
        gl.glGetPerfQueryDataINTEL(i, i1, i2, buffer, ints, i3);
    }

    @Override
    public void glGetPerfQueryIdByNameINTEL(ByteBuffer byteBuffer, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetPerfQueryIdByNameINTEL(byteBuffer, intBuffer);
    }

    @Override
    public void glGetPerfQueryIdByNameINTEL(byte[] bytes, int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGetPerfQueryIdByNameINTEL(bytes, i, ints, i1);
    }

    @Override
    public void glGetPerfQueryInfoINTEL(int i, int i1, ByteBuffer byteBuffer, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, IntBuffer intBuffer3) {
        verifyAccess();
        gl.glGetPerfQueryInfoINTEL(i, i1, byteBuffer, intBuffer, intBuffer1, intBuffer2, intBuffer3);
    }

    @Override
    public void glGetPerfQueryInfoINTEL(int i, int i1, byte[] bytes, int i2, int[] ints, int i3, int[] ints1, int i4, int[] ints2, int i5, int[] ints3, int i6) {
        verifyAccess();
        gl.glGetPerfQueryInfoINTEL(i, i1, bytes, i2, ints, i3, ints1, i4, ints2, i5, ints3, i6);
    }

    @Override
    public void glBeginConditionalRenderNVX(int i) {
        verifyAccess();
        gl.glBeginConditionalRenderNVX(i);
    }

    @Override
    public void glEndConditionalRenderNVX() {
        verifyAccess();
        gl.glEndConditionalRenderNVX();
    }

    @Override
    public void glMultiDrawArraysIndirectBindlessNV(int i, Buffer buffer, int i1, int i2, int i3) {
        verifyAccess();
        gl.glMultiDrawArraysIndirectBindlessNV(i, buffer, i1, i2, i3);
    }

    @Override
    public void glMultiDrawElementsIndirectBindlessNV(int i, int i1, Buffer buffer, int i2, int i3, int i4) {
        verifyAccess();
        gl.glMultiDrawElementsIndirectBindlessNV(i, i1, buffer, i2, i3, i4);
    }

    @Override
    public void glMultiDrawArraysIndirectBindlessCountNV(int i, Buffer buffer, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glMultiDrawArraysIndirectBindlessCountNV(i, buffer, i1, i2, i3, i4);
    }

    @Override
    public void glMultiDrawElementsIndirectBindlessCountNV(int i, int i1, Buffer buffer, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glMultiDrawElementsIndirectBindlessCountNV(i, i1, buffer, i2, i3, i4, i5);
    }

    @Override
    public void glCreateStatesNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glCreateStatesNV(i, intBuffer);
    }

    @Override
    public void glCreateStatesNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glCreateStatesNV(i, ints, i1);
    }

    @Override
    public void glDeleteStatesNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteStatesNV(i, intBuffer);
    }

    @Override
    public void glDeleteStatesNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeleteStatesNV(i, ints, i1);
    }

    @Override
    public boolean glIsStateNV(int i) {
        verifyAccess();
        return gl.glIsStateNV(i);
    }

    @Override
    public void glStateCaptureNV(int i, int i1) {
        verifyAccess();
        gl.glStateCaptureNV(i, i1);
    }

    @Override
    public int glGetCommandHeaderNV(int i, int i1) {
        verifyAccess();
        return gl.glGetCommandHeaderNV(i, i1);
    }

    @Override
    public short glGetStageIndexNV(int i) {
        return gl.glGetStageIndexNV(i);
    }

    @Override
    public void glDrawCommandsNV(int i, int i1, PointerBuffer pointerBuffer, IntBuffer intBuffer, int i2) {
        verifyAccess();
        gl.glDrawCommandsNV(i, i1, pointerBuffer, intBuffer, i2);
    }

    @Override
    public void glDrawCommandsNV(int i, int i1, PointerBuffer pointerBuffer, int[] ints, int i2, int i3) {
        verifyAccess();
        gl.glDrawCommandsNV(i, i1, pointerBuffer, ints, i2, i3);
    }

    @Override
    public void glDrawCommandsAddressNV(int i, LongBuffer longBuffer, IntBuffer intBuffer, int i1) {
        verifyAccess();
        gl.glDrawCommandsAddressNV(i, longBuffer, intBuffer, i1);
    }

    @Override
    public void glDrawCommandsAddressNV(int i, long[] longs, int i1, int[] ints, int i2, int i3) {
        verifyAccess();
        gl.glDrawCommandsAddressNV(i, longs, i1, ints, i2, i3);
    }

    @Override
    public void glDrawCommandsStatesNV(int i, PointerBuffer pointerBuffer, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, int i1) {
        verifyAccess();
        gl.glDrawCommandsStatesNV(i, pointerBuffer, intBuffer, intBuffer1, intBuffer2, i1);
    }

    @Override
    public void glDrawCommandsStatesNV(int i, PointerBuffer pointerBuffer, int[] ints, int i1, int[] ints1, int i2, int[] ints2, int i3, int i4) {
        verifyAccess();
        gl.glDrawCommandsStatesNV(i, pointerBuffer, ints, i1, ints1, i2, ints2, i3, i4);
    }

    @Override
    public void glDrawCommandsStatesAddressNV(LongBuffer longBuffer, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, int i) {
        verifyAccess();
        gl.glDrawCommandsStatesAddressNV(longBuffer, intBuffer, intBuffer1, intBuffer2, i);
    }

    @Override
    public void glDrawCommandsStatesAddressNV(long[] longs, int i, int[] ints, int i1, int[] ints1, int i2, int[] ints2, int i3, int i4) {
        verifyAccess();
        gl.glDrawCommandsStatesAddressNV(longs, i, ints, i1, ints1, i2, ints2, i3, i4);
    }

    @Override
    public void glCreateCommandListsNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glCreateCommandListsNV(i, intBuffer);
    }

    @Override
    public void glCreateCommandListsNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glCreateCommandListsNV(i, ints, i1);
    }

    @Override
    public void glDeleteCommandListsNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteCommandListsNV(i, intBuffer);
    }

    @Override
    public void glDeleteCommandListsNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeleteCommandListsNV(i, ints, i1);
    }

    @Override
    public boolean glIsCommandListNV(int i) {
        verifyAccess();
        return gl.glIsCommandListNV(i);
    }

    @Override
    public void glListDrawCommandsStatesClientNV(int i, int i1, PointerBuffer pointerBuffer, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, int i2) {
        verifyAccess();
        gl.glListDrawCommandsStatesClientNV(i, i1, pointerBuffer, intBuffer, intBuffer1, intBuffer2, i2);
    }

    @Override
    public void glListDrawCommandsStatesClientNV(int i, int i1, PointerBuffer pointerBuffer, int[] ints, int i2, int[] ints1, int i3, int[] ints2, int i4, int i5) {
        verifyAccess();
        gl.glListDrawCommandsStatesClientNV(i, i1, pointerBuffer, ints, i2, ints1, i3, ints2, i4, i5);
    }

    @Override
    public void glCommandListSegmentsNV(int i, int i1) {
        verifyAccess();
        gl.glCommandListSegmentsNV(i, i1);
    }

    @Override
    public void glCompileCommandListNV(int i) {
        verifyAccess();
        gl.glCompileCommandListNV(i);
    }

    @Override
    public void glCallCommandListNV(int i) {
        verifyAccess();
        gl.glCallCommandListNV(i);
    }

    @Override
    public void glSubpixelPrecisionBiasNV(int i, int i1) {
        verifyAccess();
        gl.glSubpixelPrecisionBiasNV(i, i1);
    }

    @Override
    public void glConservativeRasterParameterfNV(int i, float v) {
        verifyAccess();
        gl.glConservativeRasterParameterfNV(i, v);
    }

    @Override
    public void glCopyImageSubDataNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14) {
        verifyAccess();
        gl.glCopyImageSubDataNV(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14);
    }

    @Override
    public void glDrawTextureNV(int i, int i1, float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8) {
        verifyAccess();
        gl.glDrawTextureNV(i, i1, v, v1, v2, v3, v4, v5, v6, v7, v8);
    }

    @Override
    public void glMapControlPointsNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b, Buffer buffer) {
        verifyAccess();
        gl.glMapControlPointsNV(i, i1, i2, i3, i4, i5, i6, b, buffer);
    }

    @Override
    public void glMapParameterivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glMapParameterivNV(i, i1, intBuffer);
    }

    @Override
    public void glMapParameterivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glMapParameterivNV(i, i1, ints, i2);
    }

    @Override
    public void glMapParameterfvNV(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glMapParameterfvNV(i, i1, floatBuffer);
    }

    @Override
    public void glMapParameterfvNV(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glMapParameterfvNV(i, i1, floats, i2);
    }

    @Override
    public void glGetMapControlPointsNV(int i, int i1, int i2, int i3, int i4, boolean b, Buffer buffer) {
        verifyAccess();
        gl.glGetMapControlPointsNV(i, i1, i2, i3, i4, b, buffer);
    }

    @Override
    public void glGetMapParameterivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMapParameterivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetMapParameterivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetMapParameterivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetMapParameterfvNV(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMapParameterfvNV(i, i1, floatBuffer);
    }

    @Override
    public void glGetMapParameterfvNV(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetMapParameterfvNV(i, i1, floats, i2);
    }

    @Override
    public void glGetMapAttribParameterivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetMapAttribParameterivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetMapAttribParameterivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetMapAttribParameterivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetMapAttribParameterfvNV(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMapAttribParameterfvNV(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetMapAttribParameterfvNV(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetMapAttribParameterfvNV(i, i1, i2, floats, i3);
    }

    @Override
    public void glEvalMapsNV(int i, int i1) {
        verifyAccess();
        gl.glEvalMapsNV(i, i1);
    }

    @Override
    public void glGetMultisamplefvNV(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetMultisamplefvNV(i, i1, floatBuffer);
    }

    @Override
    public void glGetMultisamplefvNV(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glGetMultisamplefvNV(i, i1, floats, i2);
    }

    @Override
    public void glSampleMaskIndexedNV(int i, int i1) {
        verifyAccess();
        gl.glSampleMaskIndexedNV(i, i1);
    }

    @Override
    public void glTexRenderbufferNV(int i, int i1) {
        verifyAccess();
        gl.glTexRenderbufferNV(i, i1);
    }

    @Override
    public void glFragmentCoverageColorNV(int i) {
        verifyAccess();
        gl.glFragmentCoverageColorNV(i);
    }

    @Override
    public void glCoverageModulationTableNV(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glCoverageModulationTableNV(i, floatBuffer);
    }

    @Override
    public void glCoverageModulationTableNV(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glCoverageModulationTableNV(i, floats, i1);
    }

    @Override
    public void glGetCoverageModulationTableNV(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetCoverageModulationTableNV(i, floatBuffer);
    }

    @Override
    public void glGetCoverageModulationTableNV(int i, float[] floats, int i1) {
        verifyAccess();
        gl.glGetCoverageModulationTableNV(i, floats, i1);
    }

    @Override
    public void glCoverageModulationNV(int i) {
        verifyAccess();
        gl.glCoverageModulationNV(i);
    }

    @Override
    public void glRenderbufferStorageMultisampleCoverageNV(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glRenderbufferStorageMultisampleCoverageNV(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramVertexLimitNV(int i, int i1) {
        verifyAccess();
        gl.glProgramVertexLimitNV(i, i1);
    }

    @Override
    public void glFramebufferTextureFaceEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glFramebufferTextureFaceEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glProgramLocalParameterI4iNV(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glProgramLocalParameterI4iNV(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramLocalParameterI4ivNV(i, i1, intBuffer);
    }

    @Override
    public void glProgramLocalParameterI4ivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glProgramLocalParameterI4ivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramLocalParametersI4ivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramLocalParametersI4ivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glProgramLocalParametersI4ivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramLocalParameterI4uiNV(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glProgramLocalParameterI4uiNV(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramLocalParameterI4uivNV(i, i1, intBuffer);
    }

    @Override
    public void glProgramLocalParameterI4uivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glProgramLocalParameterI4uivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramLocalParametersI4uivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramLocalParametersI4uivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glProgramLocalParametersI4uivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramEnvParameterI4iNV(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glProgramEnvParameterI4iNV(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramEnvParameterI4ivNV(i, i1, intBuffer);
    }

    @Override
    public void glProgramEnvParameterI4ivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glProgramEnvParameterI4ivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramEnvParametersI4ivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramEnvParametersI4ivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glProgramEnvParametersI4ivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramEnvParameterI4uiNV(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glProgramEnvParameterI4uiNV(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramEnvParameterI4uivNV(i, i1, intBuffer);
    }

    @Override
    public void glProgramEnvParameterI4uivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glProgramEnvParameterI4uivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramEnvParametersI4uivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramEnvParametersI4uivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glProgramEnvParametersI4uivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramLocalParameterIivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramLocalParameterIivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramLocalParameterIivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramLocalParameterIuivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramLocalParameterIuivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramLocalParameterIuivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramEnvParameterIivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramEnvParameterIivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramEnvParameterIivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramEnvParameterIuivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramEnvParameterIuivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramEnvParameterIuivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramSubroutineParametersuivNV(i, i1, intBuffer);
    }

    @Override
    public void glProgramSubroutineParametersuivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glProgramSubroutineParametersuivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetProgramSubroutineParameteruivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramSubroutineParameteruivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetProgramSubroutineParameteruivNV(i, i1, ints, i2);
    }

    @Override
    public void glVertex2h(short i, short i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2h];
        gl.glVertex2h(i, i1);
    }

    @Override
    public void glVertex2hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2hv];
        gl.glVertex2hv(shortBuffer);
    }

    @Override
    public void glVertex2hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex2hv];
        gl.glVertex2hv(shorts, i);
    }

    @Override
    public void glVertex3h(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3h];
        gl.glVertex3h(i, i1, i2);
    }

    @Override
    public void glVertex3hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3hv];
        gl.glVertex3hv(shortBuffer);
    }

    @Override
    public void glVertex3hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex3hv];
        gl.glVertex3hv(shorts, i);
    }

    @Override
    public void glVertex4h(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4h];
        gl.glVertex4h(i, i1, i2, i3);
    }

    @Override
    public void glVertex4hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4hv];
        gl.glVertex4hv(shortBuffer);
    }

    @Override
    public void glVertex4hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glVertex4hv];
        gl.glVertex4hv(shorts, i);
    }

    @Override
    public void glNormal3h(short i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3h];
        gl.glNormal3h(i, i1, i2);
    }

    @Override
    public void glNormal3hv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3hv];
        gl.glNormal3hv(shortBuffer);
    }

    @Override
    public void glNormal3hv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3hv];
        gl.glNormal3hv(shorts, i);
    }

    @Override
    public void glColor3h(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3h];
        gl.glColor3h(i, i1, i2);
    }

    @Override
    public void glColor3hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3hv];
        gl.glColor3hv(shortBuffer);
    }

    @Override
    public void glColor3hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor3hv];
        gl.glColor3hv(shorts, i);
    }

    @Override
    public void glColor4h(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4h];
        gl.glColor4h(i, i1, i2, i3);
    }

    @Override
    public void glColor4hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4hv];
        gl.glColor4hv(shortBuffer);
    }

    @Override
    public void glColor4hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4hv];
        gl.glColor4hv(shorts, i);
    }

    @Override
    public void glTexCoord1h(short i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1h];
        gl.glTexCoord1h(i);
    }

    @Override
    public void glTexCoord1hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1hv];
        gl.glTexCoord1hv(shortBuffer);
    }

    @Override
    public void glTexCoord1hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord1hv];
        gl.glTexCoord1hv(shorts, i);
    }

    @Override
    public void glTexCoord2h(short i, short i1) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2h];
        gl.glTexCoord2h(i, i1);
    }

    @Override
    public void glTexCoord2hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2hv];
        gl.glTexCoord2hv(shortBuffer);
    }

    @Override
    public void glTexCoord2hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord2hv];
        gl.glTexCoord2hv(shorts, i);
    }

    @Override
    public void glTexCoord3h(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3h];
        gl.glTexCoord3h(i, i1, i2);
    }

    @Override
    public void glTexCoord3hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3hv];
        gl.glTexCoord3hv(shortBuffer);
    }

    @Override
    public void glTexCoord3hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord3hv];
        gl.glTexCoord3hv(shorts, i);
    }

    @Override
    public void glTexCoord4h(short i, short i1, short i2, short i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4h];
        gl.glTexCoord4h(i, i1, i2, i3);
    }

    @Override
    public void glTexCoord4hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4hv];
        gl.glTexCoord4hv(shortBuffer);
    }

    @Override
    public void glTexCoord4hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glTexCoord4hv];
        gl.glTexCoord4hv(shorts, i);
    }

    @Override
    public void glMultiTexCoord1h(int i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1h];
        gl.glMultiTexCoord1h(i, i1);
    }

    @Override
    public void glMultiTexCoord1hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1hv];
        gl.glMultiTexCoord1hv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord1hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord1hv];
        gl.glMultiTexCoord1hv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord2h(int i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2h];
        gl.glMultiTexCoord2h(i, i1, i2);
    }

    @Override
    public void glMultiTexCoord2hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2hv];
        gl.glMultiTexCoord2hv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord2hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord2hv];
        gl.glMultiTexCoord2hv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord3h(int i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3h];
        gl.glMultiTexCoord3h(i, i1, i2, i3);
    }

    @Override
    public void glMultiTexCoord3hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3hv];
        gl.glMultiTexCoord3hv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord3hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord3hv];
        gl.glMultiTexCoord3hv(i, shorts, i1);
    }

    @Override
    public void glMultiTexCoord4h(int i, short i1, short i2, short i3, short i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4h];
        gl.glMultiTexCoord4h(i, i1, i2, i3, i4);
    }

    @Override
    public void glMultiTexCoord4hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4hv];
        gl.glMultiTexCoord4hv(i, shortBuffer);
    }

    @Override
    public void glMultiTexCoord4hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4hv];
        gl.glMultiTexCoord4hv(i, shorts, i1);
    }

    @Override
    public void glFogCoordh(short i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordh];
        gl.glFogCoordh(i);
    }

    @Override
    public void glFogCoordhv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordhv];
        gl.glFogCoordhv(shortBuffer);
    }

    @Override
    public void glFogCoordhv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glFogCoordhv];
        gl.glFogCoordhv(shorts, i);
    }

    @Override
    public void glSecondaryColor3h(short i, short i1, short i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3h];
        gl.glSecondaryColor3h(i, i1, i2);
    }

    @Override
    public void glSecondaryColor3hv(ShortBuffer shortBuffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3hv];
        gl.glSecondaryColor3hv(shortBuffer);
    }

    @Override
    public void glSecondaryColor3hv(short[] shorts, int i) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glSecondaryColor3hv];
        gl.glSecondaryColor3hv(shorts, i);
    }

    @Override
    public void glVertexWeighth(short i) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexWeighth];
        gl.glVertexWeighth(i);
    }

    @Override
    public void glVertexWeighthv(ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexWeighthv];
        gl.glVertexWeighthv(shortBuffer);
    }

    @Override
    public void glVertexWeighthv(short[] shorts, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexWeighthv];
        gl.glVertexWeighthv(shorts, i);
    }

    @Override
    public void glVertexAttrib1h(int i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1h];
        gl.glVertexAttrib1h(i, i1);
    }

    @Override
    public void glVertexAttrib1hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1hv];
        gl.glVertexAttrib1hv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib1hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1hv];
        gl.glVertexAttrib1hv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib2h(int i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2h];
        gl.glVertexAttrib2h(i, i1, i2);
    }

    @Override
    public void glVertexAttrib2hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2hv];
        gl.glVertexAttrib2hv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib2hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2hv];
        gl.glVertexAttrib2hv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib3h(int i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3h];
        gl.glVertexAttrib3h(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttrib3hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3hv];
        gl.glVertexAttrib3hv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib3hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3hv];
        gl.glVertexAttrib3hv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4h(int i, short i1, short i2, short i3, short i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4h];
        gl.glVertexAttrib4h(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttrib4hv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4hv];
        gl.glVertexAttrib4hv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4hv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4hv];
        gl.glVertexAttrib4hv(i, shorts, i1);
    }

    @Override
    public void glVertexAttribs1hv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs1hv];
        gl.glVertexAttribs1hv(i, i1, shortBuffer);
    }

    @Override
    public void glVertexAttribs1hv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs1hv];
        gl.glVertexAttribs1hv(i, i1, shorts, i2);
    }

    @Override
    public void glVertexAttribs2hv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs2hv];
        gl.glVertexAttribs2hv(i, i1, shortBuffer);
    }

    @Override
    public void glVertexAttribs2hv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs2hv];
        gl.glVertexAttribs2hv(i, i1, shorts, i2);
    }

    @Override
    public void glVertexAttribs3hv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs3hv];
        gl.glVertexAttribs3hv(i, i1, shortBuffer);
    }

    @Override
    public void glVertexAttribs3hv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs3hv];
        gl.glVertexAttribs3hv(i, i1, shorts, i2);
    }

    @Override
    public void glVertexAttribs4hv(int i, int i1, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs4hv];
        gl.glVertexAttribs4hv(i, i1, shortBuffer);
    }

    @Override
    public void glVertexAttribs4hv(int i, int i1, short[] shorts, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribs4hv];
        gl.glVertexAttribs4hv(i, i1, shorts, i2);
    }

    @Override
    public void glGenOcclusionQueriesNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGenOcclusionQueriesNV(i, intBuffer);
    }

    @Override
    public void glGenOcclusionQueriesNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGenOcclusionQueriesNV(i, ints, i1);
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteOcclusionQueriesNV(i, intBuffer);
    }

    @Override
    public void glDeleteOcclusionQueriesNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeleteOcclusionQueriesNV(i, ints, i1);
    }

    @Override
    public boolean glIsOcclusionQueryNV(int i) {
        verifyAccess();
        return gl.glIsOcclusionQueryNV(i);
    }

    @Override
    public void glBeginOcclusionQueryNV(int i) {
        verifyAccess();
        gl.glBeginOcclusionQueryNV(i);
    }

    @Override
    public void glEndOcclusionQueryNV() {
        verifyAccess();
        gl.glEndOcclusionQueryNV();
    }

    @Override
    public void glGetOcclusionQueryivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetOcclusionQueryivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetOcclusionQueryivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetOcclusionQueryivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetOcclusionQueryuivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetOcclusionQueryuivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetOcclusionQueryuivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetOcclusionQueryuivNV(i, i1, ints, i2);
    }

    @Override
    public void glProgramBufferParametersfvNV(int i, int i1, int i2, int i3, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glProgramBufferParametersfvNV(i, i1, i2, i3, floatBuffer);
    }

    @Override
    public void glProgramBufferParametersfvNV(int i, int i1, int i2, int i3, float[] floats, int i4) {
        verifyAccess();
        gl.glProgramBufferParametersfvNV(i, i1, i2, i3, floats, i4);
    }

    @Override
    public void glProgramBufferParametersIivNV(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramBufferParametersIivNV(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glProgramBufferParametersIivNV(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glProgramBufferParametersIivNV(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glProgramBufferParametersIuivNV(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        gl.glProgramBufferParametersIuivNV(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glProgramBufferParametersIuivNV(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        gl.glProgramBufferParametersIuivNV(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glPixelDataRangeNV(int i, int i1, Buffer buffer) {
        verifyAccess();
        gl.glPixelDataRangeNV(i, i1, buffer);
    }

    @Override
    public void glFlushPixelDataRangeNV(int i) {
        verifyAccess();
        gl.glFlushPixelDataRangeNV(i);
    }

    @Override
    public void glPrimitiveRestartNV() {
        verifyAccess();
        gl.glPrimitiveRestartNV();
    }

    @Override
    public void glPrimitiveRestartIndexNV(int i) {
        verifyAccess();
        gl.glPrimitiveRestartIndexNV(i);
    }

    @Override
    public void glFramebufferSampleLocationsfvNV(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glFramebufferSampleLocationsfvNV(i, i1, i2, floatBuffer);
    }

    @Override
    public void glFramebufferSampleLocationsfvNV(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glFramebufferSampleLocationsfvNV(i, i1, i2, floats, i3);
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvNV(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glNamedFramebufferSampleLocationsfvNV(i, i1, i2, floatBuffer);
    }

    @Override
    public void glNamedFramebufferSampleLocationsfvNV(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glNamedFramebufferSampleLocationsfvNV(i, i1, i2, floats, i3);
    }

    @Override
    public void glResolveDepthValuesNV() {
        verifyAccess();
        gl.glResolveDepthValuesNV();
    }

    @Override
    public void glTextureBarrierNV() {
        verifyAccess();
        gl.glTextureBarrierNV();
    }

    @Override
    public void glBindTransformFeedbackNV(int i, int i1) {
        verifyAccess();
        gl.glBindTransformFeedbackNV(i, i1);
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glDeleteTransformFeedbacksNV(i, intBuffer);
    }

    @Override
    public void glDeleteTransformFeedbacksNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glDeleteTransformFeedbacksNV(i, ints, i1);
    }

    @Override
    public void glGenTransformFeedbacksNV(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGenTransformFeedbacksNV(i, intBuffer);
    }

    @Override
    public void glGenTransformFeedbacksNV(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glGenTransformFeedbacksNV(i, ints, i1);
    }

    @Override
    public boolean glIsTransformFeedbackNV(int i) {
        verifyAccess();
        return gl.glIsTransformFeedbackNV(i);
    }

    @Override
    public void glPauseTransformFeedbackNV() {
        verifyAccess();
        gl.glPauseTransformFeedbackNV();
    }

    @Override
    public void glResumeTransformFeedbackNV() {
        verifyAccess();
        gl.glResumeTransformFeedbackNV();
    }

    @Override
    public void glDrawTransformFeedbackNV(int i, int i1) {
        verifyAccess();
        gl.glDrawTransformFeedbackNV(i, i1);
    }

    @Override
    public void glVDPAUInitNV(Buffer buffer, Buffer buffer1) {
        verifyAccess();
        gl.glVDPAUInitNV(buffer, buffer1);
    }

    @Override
    public void glVDPAUFiniNV() {
        verifyAccess();
        gl.glVDPAUFiniNV();
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer buffer, int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        return gl.glVDPAURegisterVideoSurfaceNV(buffer, i, i1, intBuffer);
    }

    @Override
    public long glVDPAURegisterVideoSurfaceNV(Buffer buffer, int i, int i1, int[] ints, int i2) {
        verifyAccess();
        return gl.glVDPAURegisterVideoSurfaceNV(buffer, i, i1, ints, i2);
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer buffer, int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        return gl.glVDPAURegisterOutputSurfaceNV(buffer, i, i1, intBuffer);
    }

    @Override
    public long glVDPAURegisterOutputSurfaceNV(Buffer buffer, int i, int i1, int[] ints, int i2) {
        verifyAccess();
        return gl.glVDPAURegisterOutputSurfaceNV(buffer, i, i1, ints, i2);
    }

    @Override
    public boolean glVDPAUIsSurfaceNV(long l) {
        verifyAccess();
        return gl.glVDPAUIsSurfaceNV(l);
    }

    @Override
    public void glVDPAUUnregisterSurfaceNV(long l) {
        verifyAccess();
        gl.glVDPAUUnregisterSurfaceNV(l);
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long l, int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        gl.glVDPAUGetSurfaceivNV(l, i, i1, intBuffer, intBuffer1);
    }

    @Override
    public void glVDPAUGetSurfaceivNV(long l, int i, int i1, int[] ints, int i2, int[] ints1, int i3) {
        verifyAccess();
        gl.glVDPAUGetSurfaceivNV(l, i, i1, ints, i2, ints1, i3);
    }

    @Override
    public void glVDPAUSurfaceAccessNV(long l, int i) {
        verifyAccess();
        gl.glVDPAUSurfaceAccessNV(l, i);
    }

    @Override
    public void glVDPAUMapSurfacesNV(int i, PointerBuffer pointerBuffer) {
        verifyAccess();
        gl.glVDPAUMapSurfacesNV(i, pointerBuffer);
    }

    @Override
    public void glVDPAUUnmapSurfacesNV(int i, PointerBuffer pointerBuffer) {
        verifyAccess();
        gl.glVDPAUUnmapSurfacesNV(i, pointerBuffer);
    }

    @Override
    public void glVertexAttribL1i64NV(int i, long l) {
        verifyAccess();
        gl.glVertexAttribL1i64NV(i, l);
    }

    @Override
    public void glVertexAttribL2i64NV(int i, long l, long l1) {
        verifyAccess();
        gl.glVertexAttribL2i64NV(i, l, l1);
    }

    @Override
    public void glVertexAttribL3i64NV(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glVertexAttribL3i64NV(i, l, l1, l2);
    }

    @Override
    public void glVertexAttribL4i64NV(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glVertexAttribL4i64NV(i, l, l1, l2, l3);
    }

    @Override
    public void glVertexAttribL1i64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL1i64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL1i64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL1i64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL2i64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL2i64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL2i64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL2i64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL3i64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL3i64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL3i64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL3i64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL4i64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL4i64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL4i64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL4i64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL1ui64NV(int i, long l) {
        verifyAccess();
        gl.glVertexAttribL1ui64NV(i, l);
    }

    @Override
    public void glVertexAttribL2ui64NV(int i, long l, long l1) {
        verifyAccess();
        gl.glVertexAttribL2ui64NV(i, l, l1);
    }

    @Override
    public void glVertexAttribL3ui64NV(int i, long l, long l1, long l2) {
        verifyAccess();
        gl.glVertexAttribL3ui64NV(i, l, l1, l2);
    }

    @Override
    public void glVertexAttribL4ui64NV(int i, long l, long l1, long l2, long l3) {
        verifyAccess();
        gl.glVertexAttribL4ui64NV(i, l, l1, l2, l3);
    }

    @Override
    public void glVertexAttribL1ui64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL1ui64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL1ui64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL1ui64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL2ui64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL2ui64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL2ui64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL2ui64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL3ui64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL3ui64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL3ui64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL3ui64vNV(i, longs, i1);
    }

    @Override
    public void glVertexAttribL4ui64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glVertexAttribL4ui64vNV(i, longBuffer);
    }

    @Override
    public void glVertexAttribL4ui64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glVertexAttribL4ui64vNV(i, longs, i1);
    }

    @Override
    public void glGetVertexAttribLi64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetVertexAttribLi64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetVertexAttribLi64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetVertexAttribLi64vNV(i, i1, longs, i2);
    }

    @Override
    public void glGetVertexAttribLui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetVertexAttribLui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetVertexAttribLui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetVertexAttribLui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glVertexAttribLFormatNV(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glVertexAttribLFormatNV(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttribI1iEXT(int i, int i1) {
        verifyAccess();
        gl.glVertexAttribI1iEXT(i, i1);
    }

    @Override
    public void glVertexAttribI2iEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexAttribI2iEXT(i, i1, i2);
    }

    @Override
    public void glVertexAttribI3iEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glVertexAttribI3iEXT(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttribI4iEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glVertexAttribI4iEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttribI1uiEXT(int i, int i1) {
        verifyAccess();
        gl.glVertexAttribI1uiEXT(i, i1);
    }

    @Override
    public void glVertexAttribI2uiEXT(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexAttribI2uiEXT(i, i1, i2);
    }

    @Override
    public void glVertexAttribI3uiEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glVertexAttribI3uiEXT(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttribI4uiEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glVertexAttribI4uiEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttribI1ivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI1ivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI1ivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI1ivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI2ivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI2ivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI2ivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI2ivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI3ivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI3ivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI3ivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI3ivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI4ivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI4ivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI4ivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI4ivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI1uivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI1uivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI1uivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI1uivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI2uivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI2uivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI2uivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI2uivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI3uivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI3uivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI3uivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI3uivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI4uivEXT(int i, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVertexAttribI4uivEXT(i, intBuffer);
    }

    @Override
    public void glVertexAttribI4uivEXT(int i, int[] ints, int i1) {
        verifyAccess();
        gl.glVertexAttribI4uivEXT(i, ints, i1);
    }

    @Override
    public void glVertexAttribI4bvEXT(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttribI4bvEXT(i, byteBuffer);
    }

    @Override
    public void glVertexAttribI4bvEXT(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttribI4bvEXT(i, bytes, i1);
    }

    @Override
    public void glVertexAttribI4svEXT(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttribI4svEXT(i, shortBuffer);
    }

    @Override
    public void glVertexAttribI4svEXT(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttribI4svEXT(i, shorts, i1);
    }

    @Override
    public void glVertexAttribI4ubvEXT(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        gl.glVertexAttribI4ubvEXT(i, byteBuffer);
    }

    @Override
    public void glVertexAttribI4ubvEXT(int i, byte[] bytes, int i1) {
        verifyAccess();
        gl.glVertexAttribI4ubvEXT(i, bytes, i1);
    }

    @Override
    public void glVertexAttribI4usvEXT(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        gl.glVertexAttribI4usvEXT(i, shortBuffer);
    }

    @Override
    public void glVertexAttribI4usvEXT(int i, short[] shorts, int i1) {
        verifyAccess();
        gl.glVertexAttribI4usvEXT(i, shorts, i1);
    }

    @Override
    public void glVertexAttribIPointerEXT(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        gl.glVertexAttribIPointerEXT(i, i1, i2, i3, buffer);
    }

    @Override
    public void glGetVertexAttribIivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVertexAttribIivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribIivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVertexAttribIivEXT(i, i1, ints, i2);
    }

    @Override
    public void glGetVertexAttribIuivEXT(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVertexAttribIuivEXT(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribIuivEXT(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVertexAttribIuivEXT(i, i1, ints, i2);
    }

    @Override
    public void glBeginVideoCaptureNV(int i) {
        verifyAccess();
        gl.glBeginVideoCaptureNV(i);
    }

    @Override
    public void glBindVideoCaptureStreamBufferNV(int i, int i1, int i2, long l) {
        verifyAccess();
        gl.glBindVideoCaptureStreamBufferNV(i, i1, i2, l);
    }

    @Override
    public void glBindVideoCaptureStreamTextureNV(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glBindVideoCaptureStreamTextureNV(i, i1, i2, i3, i4);
    }

    @Override
    public void glEndVideoCaptureNV(int i) {
        verifyAccess();
        gl.glEndVideoCaptureNV(i);
    }

    @Override
    public void glGetVideoCaptureivNV(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVideoCaptureivNV(i, i1, intBuffer);
    }

    @Override
    public void glGetVideoCaptureivNV(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        gl.glGetVideoCaptureivNV(i, i1, ints, i2);
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glGetVideoCaptureStreamivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetVideoCaptureStreamivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glGetVideoCaptureStreamivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glGetVideoCaptureStreamfvNV(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetVideoCaptureStreamfvNV(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glGetVideoCaptureStreamfvNV(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glGetVideoCaptureStreamdvNV(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glGetVideoCaptureStreamdvNV(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glGetVideoCaptureStreamdvNV(i, i1, i2, doubles, i3);
    }

    @Override
    public int glVideoCaptureNV(int i, IntBuffer intBuffer, LongBuffer longBuffer) {
        verifyAccess();
        return gl.glVideoCaptureNV(i, intBuffer, longBuffer);
    }

    @Override
    public int glVideoCaptureNV(int i, int[] ints, int i1, long[] longs, int i2) {
        verifyAccess();
        return gl.glVideoCaptureNV(i, ints, i1, longs, i2);
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterivNV(i, i1, i2, intBuffer);
    }

    @Override
    public void glVideoCaptureStreamParameterivNV(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterivNV(i, i1, i2, ints, i3);
    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterfvNV(i, i1, i2, floatBuffer);
    }

    @Override
    public void glVideoCaptureStreamParameterfvNV(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterfvNV(i, i1, i2, floats, i3);
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterdvNV(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glVideoCaptureStreamParameterdvNV(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        gl.glVideoCaptureStreamParameterdvNV(i, i1, i2, doubles, i3);
    }

    @Override
    public void glFramebufferTextureMultiviewOVR(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glFramebufferTextureMultiviewOVR(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glHintPGI(int i, int i1) {
        verifyAccess();
        gl.glHintPGI(i, i1);
    }

    @Override
    public void glFinishTextureSUNX() {
        verifyAccess();
        gl.glFinishTextureSUNX();
    }

    @Override
    public GLBufferStorage mapNamedBufferEXT(int i, int i1) throws GLException {
        return gl.mapNamedBufferEXT(i, i1);
    }

    @Override
    public GLBufferStorage mapNamedBufferRangeEXT(int i, long l, long l1, int i1) throws GLException {
        return gl.mapNamedBufferRangeEXT(i, l, l1, i1);
    }

    @Override
    public void glVertexAttribPointer(int i, int i1, int i2, boolean b, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribPointer];
        gl.glVertexAttribPointer(i, i1, i2, b, i3, buffer);
    }

    @Override
    public void glDrawElementsInstanced(int i, int i1, int i2, Buffer buffer, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElementsInstanced];
        gl.glDrawElementsInstanced(i, i1, i2, buffer, i3);
    }

    @Override
    public void glDrawRangeElements(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawRangeElements];
        gl.glDrawRangeElements(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glVertexAttribIPointer(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribIPointer];
        gl.glVertexAttribIPointer(i, i1, i2, i3, buffer);
    }

    @Override
    public void glAlphaFunc(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glAlphaFunc];
        gl.glAlphaFunc(i, v);
    }

    @Override
    public void glFogf(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogf];
        gl.glFogf(i, v);
    }

    @Override
    public void glFogfv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogfv];
        gl.glFogfv(i, floatBuffer);
    }

    @Override
    public void glFogfv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glFogfv];
        gl.glFogfv(i, floats, i1);
    }

    @Override
    public void glGetLightfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetLightfv];
        gl.glGetLightfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetLightfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetLightfv];
        gl.glGetLightfv(i, i1, floats, i2);
    }

    @Override
    public void glGetMaterialfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMaterialfv];
        gl.glGetMaterialfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetMaterialfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMaterialfv];
        gl.glGetMaterialfv(i, i1, floats, i2);
    }

    @Override
    public void glGetTexEnvfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexEnvfv];
        gl.glGetTexEnvfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetTexEnvfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexEnvfv];
        gl.glGetTexEnvfv(i, i1, floats, i2);
    }

    @Override
    public void glLightModelf(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModelf];
        gl.glLightModelf(i, v);
    }

    @Override
    public void glLightModelfv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModelfv];
        gl.glLightModelfv(i, floatBuffer);
    }

    @Override
    public void glLightModelfv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightModelfv];
        gl.glLightModelfv(i, floats, i1);
    }

    @Override
    public void glLightf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightf];
        gl.glLightf(i, i1, v);
    }

    @Override
    public void glMultiTexCoord4f(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiTexCoord4f];
        gl.glMultiTexCoord4f(i, v, v1, v2, v3);
    }

    @Override
    public void glNormal3f(float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormal3f];
        gl.glNormal3f(v, v1, v2);
    }

    @Override
    public void glPointParameterf(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameterf];
        gl.glPointParameterf(i, v);
    }

    @Override
    public void glPointParameterfv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameterfv];
        gl.glPointParameterfv(i, floatBuffer);
    }

    @Override
    public void glPointParameterfv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameterfv];
        gl.glPointParameterfv(i, floats, i1);
    }

    @Override
    public void glPointSize(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointSize];
        gl.glPointSize(v);
    }

    @Override
    public void glTexEnvf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnvf];
        gl.glTexEnvf(i, i1, v);
    }

    @Override
    public void glTexEnvfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnvfv];
        gl.glTexEnvfv(i, i1, floatBuffer);
    }

    @Override
    public void glTexEnvfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnvfv];
        gl.glTexEnvfv(i, i1, floats, i2);
    }

    @Override
    public void glClientActiveTexture(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glClientActiveTexture];
        gl.glClientActiveTexture(i);
    }

    @Override
    public void glColor4ub(byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4ub];
        gl.glColor4ub(b, b1, b2, b3);
    }

    @Override
    public void glGetTexEnviv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexEnviv];
        gl.glGetTexEnviv(i, i1, intBuffer);
    }

    @Override
    public void glGetTexEnviv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexEnviv];
        gl.glGetTexEnviv(i, i1, ints, i2);
    }

    @Override
    public void glLogicOp(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLogicOp];
        gl.glLogicOp(i);
    }

    @Override
    public void glTexEnvi(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnvi];
        gl.glTexEnvi(i, i1, i2);
    }

    @Override
    public void glTexEnviv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnviv];
        gl.glTexEnviv(i, i1, intBuffer);
    }

    @Override
    public void glTexEnviv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexEnviv];
        gl.glTexEnviv(i, i1, ints, i2);
    }

    @Override
    public void glOrtho(double v, double v1, double v2, double v3, double v4, double v5) {
        verifyAccess();
        ++methodCalls[GLFunc.glOrtho];
        gl.glOrtho(v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glFrustum(double v, double v1, double v2, double v3, double v4, double v5) {
        verifyAccess();
        ++methodCalls[GLFunc.glFrustum];
        gl.glFrustum(v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glDrawElements(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElements];
        gl.glDrawElements(i, i1, i2, buffer);
    }

    @Override
    public void glActiveTexture(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glActiveTexture];
        gl.glActiveTexture(i);
    }

    @Override
    public void glBindBuffer(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindBuffer];
        gl.glBindBuffer(i, i1);
    }

    @Override
    public void glBindFramebuffer(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindFramebuffer];
        gl.glBindFramebuffer(i, i1);
    }

    @Override
    public void glBindRenderbuffer(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindRenderbuffer];
        gl.glBindRenderbuffer(i, i1);
    }

    @Override
    public void glBindTexture(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindTexture];
        gl.glBindTexture(i, i1);
    }

    @Override
    public void glBlendEquation(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendEquation];
        gl.glBlendEquation(i);
    }

    @Override
    public void glBlendEquationSeparate(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendEquationSeparate];
        gl.glBlendEquationSeparate(i, i1);
    }

    @Override
    public void glBlendFunc(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendFunc];
        gl.glBlendFunc(i, i1);
    }

    @Override
    public void glBlendFuncSeparate(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendFuncSeparate];
        gl.glBlendFuncSeparate(i, i1, i2, i3);
    }

    @Override
    public void glBufferData(int i, long l, Buffer buffer, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBufferData];
        gl.glBufferData(i, l, buffer, i1);
    }

    @Override
    public void glBufferSubData(int i, long l, long l1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glBufferSubData];
        gl.glBufferSubData(i, l, l1, buffer);
    }

    @Override
    public int glCheckFramebufferStatus(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glCheckFramebufferStatus];
        return gl.glCheckFramebufferStatus(i);
    }

    @Override
    public void glClear(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glClear];
        gl.glClear(i);
    }

    @Override
    public void glClearColor(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearColor];
        gl.glClearColor(v, v1, v2, v3);
    }

    @Override
    public void glClearDepthf(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearDepthf];
        gl.glClearDepthf(v);
    }

    @Override
    public void glClearStencil(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearStencil];
        gl.glClearStencil(i);
    }

    @Override
    public void glColorMask(boolean b, boolean b1, boolean b2, boolean b3) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorMask];
        gl.glColorMask(b, b1, b2, b3);
    }

    @Override
    public void glCompressedTexImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage2D];
        gl.glCompressedTexImage2D(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glCompressedTexImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage2D];
        gl.glCompressedTexImage2D(i, i1, i2, i3, i4, i5, i6, l);
    }

    @Override
    public void glCompressedTexSubImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage2D];
        gl.glCompressedTexSubImage2D(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glCompressedTexSubImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage2D];
        gl.glCompressedTexSubImage2D(i, i1, i2, i3, i4, i5, i6, i7, l);
    }

    @Override
    public void glCopyTexImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyTexImage2D];
        gl.glCopyTexImage2D(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glCopyTexSubImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyTexSubImage2D];
        gl.glCopyTexSubImage2D(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glCullFace(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glCullFace];
        gl.glCullFace(i);
    }

    @Override
    public void glDeleteBuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteBuffers];
        gl.glDeleteBuffers(i, intBuffer);
    }

    @Override
    public void glDeleteBuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteBuffers];
        gl.glDeleteBuffers(i, ints, i1);
    }

    @Override
    public void glDeleteFramebuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteFramebuffers];
        gl.glDeleteFramebuffers(i, intBuffer);
    }

    @Override
    public void glDeleteFramebuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteFramebuffers];
        gl.glDeleteFramebuffers(i, ints, i1);
    }

    @Override
    public void glDeleteRenderbuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteRenderbuffers];
        gl.glDeleteRenderbuffers(i, intBuffer);
    }

    @Override
    public void glDeleteRenderbuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteRenderbuffers];
        gl.glDeleteRenderbuffers(i, ints, i1);
    }

    @Override
    public void glDeleteTextures(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteTextures];
        gl.glDeleteTextures(i, intBuffer);
    }

    @Override
    public void glDeleteTextures(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteTextures];
        gl.glDeleteTextures(i, ints, i1);
    }

    @Override
    public void glDepthFunc(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDepthFunc];
        gl.glDepthFunc(i);
    }

    @Override
    public void glDepthMask(boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glDepthMask];
        gl.glDepthMask(b);
    }

    @Override
    public void glDepthRangef(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDepthRangef];
        gl.glDepthRangef(v, v1);
    }

    @Override
    public void glDisable(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDisable];
        gl.glDisable(i);
    }

    @Override
    public void glDrawArrays(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawArrays];
        gl.glDrawArrays(i, i1, i2);
    }

    @Override
    public void glDrawElements(int i, int i1, int i2, long l) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElements];
        gl.glDrawElements(i, i1, i2, l);
    }

    @Override
    public void glEnable(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEnable];
        gl.glEnable(i);
    }

    @Override
    public void glFinish() {
        verifyAccess();
        ++methodCalls[GLFunc.glFinish];
        gl.glFinish();
    }

    @Override
    public void glFlush() {
        verifyAccess();
        ++methodCalls[GLFunc.glFlush];
        gl.glFlush();
    }

    @Override
    public void glFramebufferRenderbuffer(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferRenderbuffer];
        gl.glFramebufferRenderbuffer(i, i1, i2, i3);
    }

    @Override
    public void glFramebufferTexture2D(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferTexture2D];
        gl.glFramebufferTexture2D(i, i1, i2, i3, i4);
    }

    @Override
    public void glFrontFace(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glFrontFace];
        gl.glFrontFace(i);
    }

    @Override
    public void glGenBuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenBuffers];
        gl.glGenBuffers(i, intBuffer);
    }

    @Override
    public void glGenBuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenBuffers];
        gl.glGenBuffers(i, ints, i1);
    }

    @Override
    public void glGenerateMipmap(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenerateMipmap];
        gl.glGenerateMipmap(i);
    }

    @Override
    public void glGenFramebuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenFramebuffers];
        gl.glGenFramebuffers(i, intBuffer);
    }

    @Override
    public void glGenFramebuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenFramebuffers];
        gl.glGenFramebuffers(i, ints, i1);
    }

    @Override
    public void glGenRenderbuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenRenderbuffers];
        gl.glGenRenderbuffers(i, intBuffer);
    }

    @Override
    public void glGenRenderbuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenRenderbuffers];
        gl.glGenRenderbuffers(i, ints, i1);
    }

    @Override
    public void glGenTextures(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenTextures];
        gl.glGenTextures(i, intBuffer);
    }

    @Override
    public void glGenTextures(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenTextures];
        gl.glGenTextures(i, ints, i1);
    }

    @Override
    public void glGetBooleanv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleanv];
        gl.glGetBooleanv(i, byteBuffer);
    }

    @Override
    public void glGetBooleanv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleanv];
        gl.glGetBooleanv(i, bytes, i1);
    }

    @Override
    public void glGetBufferParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBufferParameteriv];
        gl.glGetBufferParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetBufferParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBufferParameteriv];
        gl.glGetBufferParameteriv(i, i1, ints, i2);
    }

    @Override
    public int glGetError() {
        verifyAccess();
        ++methodCalls[GLFunc.glGetError];
        return gl.glGetError();
    }

    @Override
    public void glGetFloatv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFloatv];
        gl.glGetFloatv(i, floatBuffer);
    }

    @Override
    public void glGetFloatv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFloatv];
        gl.glGetFloatv(i, floats, i1);
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFramebufferAttachmentParameteriv];
        gl.glGetFramebufferAttachmentParameteriv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetFramebufferAttachmentParameteriv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFramebufferAttachmentParameteriv];
        gl.glGetFramebufferAttachmentParameteriv(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetIntegerv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegerv];
        gl.glGetIntegerv(i, intBuffer);
    }

    @Override
    public void glGetIntegerv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegerv];
        gl.glGetIntegerv(i, ints, i1);
    }

    @Override
    public void glGetRenderbufferParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetRenderbufferParameteriv];
        gl.glGetRenderbufferParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetRenderbufferParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetRenderbufferParameteriv];
        gl.glGetRenderbufferParameteriv(i, i1, ints, i2);
    }

    @Override
    public String glGetString(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetString];
        return gl.glGetString(i);
    }

    @Override
    public void glGetTexParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterfv];
        gl.glGetTexParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetTexParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterfv];
        gl.glGetTexParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glGetTexParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameteriv];
        gl.glGetTexParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetTexParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameteriv];
        gl.glGetTexParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glHint(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glHint];
        gl.glHint(i, i1);
    }

    @Override
    public boolean glIsBuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsBuffer];
        return gl.glIsBuffer(i);
    }

    @Override
    public boolean glIsEnabled(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsEnabled];
        return gl.glIsEnabled(i);
    }

    @Override
    public boolean glIsFramebuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsFramebuffer];
        return gl.glIsFramebuffer(i);
    }

    @Override
    public boolean glIsRenderbuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsRenderbuffer];
        return gl.glIsRenderbuffer(i);
    }

    @Override
    public boolean glIsTexture(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsTexture];
        return gl.glIsTexture(i);
    }

    @Override
    public void glLineWidth(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glLineWidth];
        gl.glLineWidth(v);
    }

    @Override
    public void glPixelStorei(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelStorei];
        gl.glPixelStorei(i, i1);
    }

    @Override
    public void glPolygonOffset(float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPolygonOffset];
        gl.glPolygonOffset(v, v1);
    }

    @Override
    public void glReadPixels(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glReadPixels];
        gl.glReadPixels(i, i1, i2, i3, i4, i5, buffer);
    }

    @Override
    public void glReadPixels(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glReadPixels];
        gl.glReadPixels(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glRenderbufferStorage(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRenderbufferStorage];
        gl.glRenderbufferStorage(i, i1, i2, i3);
    }

    @Override
    public void glSampleCoverage(float v, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glSampleCoverage];
        gl.glSampleCoverage(v, b);
    }

    @Override
    public void glScissor(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glScissor];
        gl.glScissor(i, i1, i2, i3);
    }

    @Override
    public void glStencilFunc(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilFunc];
        gl.glStencilFunc(i, i1, i2);
    }

    @Override
    public void glStencilMask(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilMask];
        gl.glStencilMask(i);
    }

    @Override
    public void glStencilOp(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilOp];
        gl.glStencilOp(i, i1, i2);
    }

    @Override
    public void glTexImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage2D];
        gl.glTexImage2D(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glTexImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage2D];
        gl.glTexImage2D(i, i1, i2, i3, i4, i5, i6, i7, l);
    }

    @Override
    public void glTexParameterf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterf];
        gl.glTexParameterf(i, i1, v);
    }

    @Override
    public void glTexParameterfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterfv];
        gl.glTexParameterfv(i, i1, floatBuffer);
    }

    @Override
    public void glTexParameterfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterfv];
        gl.glTexParameterfv(i, i1, floats, i2);
    }

    @Override
    public void glTexParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameteri];
        gl.glTexParameteri(i, i1, i2);
    }

    @Override
    public void glTexParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameteriv];
        gl.glTexParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glTexParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameteriv];
        gl.glTexParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glTexSubImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage2D];
        gl.glTexSubImage2D(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glTexSubImage2D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage2D];
        gl.glTexSubImage2D(i, i1, i2, i3, i4, i5, i6, i7, l);
    }

    @Override
    public void glViewport(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glViewport];
        gl.glViewport(i, i1, i2, i3);
    }

    @Override
    public void glTexStorage1D(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexStorage1D];
        gl.glTexStorage1D(i, i1, i2, i3);
    }

    @Override
    public void glTexStorage2D(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexStorage2D];
        gl.glTexStorage2D(i, i1, i2, i3, i4);
    }

    @Override
    public void glTexStorage3D(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexStorage3D];
        gl.glTexStorage3D(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glTextureStorage1DEXT(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        gl.glTextureStorage1DEXT(i, i1, i2, i3, i4);
    }

    @Override
    public void glTextureStorage2DEXT(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        gl.glTextureStorage2DEXT(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glTextureStorage3DEXT(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        gl.glTextureStorage3DEXT(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public ByteBuffer glMapBuffer(int i, int i1) {
        return gl.glMapBuffer(i, i1);
    }

    @Override
    public boolean glUnmapBuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glUnmapBuffer];
        return gl.glUnmapBuffer(i);
    }

    @Override
    public void glRenderbufferStorageMultisample(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glRenderbufferStorageMultisample];
        gl.glRenderbufferStorageMultisample(i, i1, i2, i3, i4);
    }

    @Override
    public ByteBuffer glMapBufferRange(int i, long l, long l1, int i1) {
        return gl.glMapBufferRange(i, l, l1, i1);
    }

    @Override
    public void glFlushMappedBufferRange(int i, long l, long l1) {
        verifyAccess();
        ++methodCalls[GLFunc.glFlushMappedBufferRange];
        gl.glFlushMappedBufferRange(i, l, l1);
    }

    @Override
    public int glGetGraphicsResetStatus() {
        verifyAccess();
        ++methodCalls[GLFunc.glGetGraphicsResetStatus];
        return gl.glGetGraphicsResetStatus();
    }

    @Override
    public void glReadnPixels(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glReadnPixels];
        gl.glReadnPixels(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glGetnUniformfv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformfv];
        gl.glGetnUniformfv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetnUniformfv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformfv];
        gl.glGetnUniformfv(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetnUniformiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformiv];
        gl.glGetnUniformiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetnUniformiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformiv];
        gl.glGetnUniformiv(i, i1, i2, ints, i3);
    }

    @Override
    public boolean isGL() {
        return gl.isGL();
    }

    @Override
    public boolean isGL4bc() {
        return false;
    }

    @Override
    public boolean isGL4() {
        return false;
    }

    @Override
    public boolean isGL3bc() {
        return false;
    }

    @Override
    public boolean isGL3() {
        return false;
    }

    @Override
    public boolean isGL2() {
        return gl.isGL2();
    }

    @Override
    public boolean isGLES1() {
        return false;
    }

    @Override
    public boolean isGLES2() {
        return false;
    }

    @Override
    public boolean isGLES3() {
        return false;
    }

    @Override
    public boolean isGLES() {
        return false;
    }

    @Override
    public boolean isGL2ES1() {
        return gl.isGL2ES1();
    }

    @Override
    public boolean isGL2ES2() {
        return gl.isGL2ES2();
    }

    @Override
    public boolean isGL2ES3() {
        return gl.isGL2ES3();
    }

    @Override
    public boolean isGL3ES3() {
        return false;
    }

    @Override
    public boolean isGL4ES3() {
        return false;
    }

    @Override
    public boolean isGL2GL3() {
        return gl.isGL2GL3();
    }

    @Override
    public boolean isGL4core() {
        return gl.isGL4core();
    }

    @Override
    public boolean isGL3core() {
        return gl.isGL3core();
    }

    @Override
    public boolean isGLcore() {
        return gl.isGLcore();
    }

    @Override
    public boolean isGLES2Compatible() {
        return gl.isGLES2Compatible();
    }

    @Override
    public boolean isGLES3Compatible() {
        return gl.isGLES3Compatible();
    }

    @Override
    public boolean isGLES31Compatible() {
        return gl.isGLES31Compatible();
    }

    @Override
    public boolean isGLES32Compatible() {
        return gl.isGLES32Compatible();
    }

    @Override
    public boolean hasGLSL() {
        return gl.hasGLSL();
    }

    @Override
    public GL getDownstreamGL() throws GLException {
        return gl.getDownstreamGL();
    }

    @Override
    public GL getRootGL() throws GLException {
        return this;
    }

    @Override
    public GL getGL() throws GLException {
        return this;
    }

    @Override
    public GL4bc getGL4bc() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL4 getGL4() throws GLException {
        return gl.getGL4();
    }

    @Override
    public GL3bc getGL3bc() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL3 getGL3() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL2 getGL2() throws GLException {
        return this;
    }

    @Override
    public GLES1 getGLES1() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GLES2 getGLES2() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GLES3 getGLES3() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL2ES1 getGL2ES1() throws GLException {
        return this;
    }

    @Override
    public GL2ES2 getGL2ES2() throws GLException {
        return this;
    }

    @Override
    public GL2ES3 getGL2ES3() throws GLException {
        return this;
    }

    @Override
    public GL3ES3 getGL3ES3() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL4ES3 getGL4ES3() throws GLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public GL2GL3 getGL2GL3() throws GLException {
        return this;
    }

    @Override
    public GLProfile getGLProfile() {
        return gl.getGLProfile();
    }

    @Override
    public GLContext getContext() {
        return gl.getContext();
    }

    @Override
    public boolean isFunctionAvailable(String s) {
        return gl.isFunctionAvailable(s);
    }

    @Override
    public boolean isExtensionAvailable(String s) {
        return gl.isExtensionAvailable(s);
    }

    @Override
    public boolean hasBasicFBOSupport() {
        return gl.hasBasicFBOSupport();
    }

    @Override
    public boolean hasFullFBOSupport() {
        return gl.hasFullFBOSupport();
    }

    @Override
    public int getMaxRenderbufferSamples() {
        return gl.getMaxRenderbufferSamples();
    }

    @Override
    public boolean isNPOTTextureAvailable() {
        return gl.isNPOTTextureAvailable();
    }

    @Override
    public boolean isTextureFormatBGRA8888Available() {
        return gl.isTextureFormatBGRA8888Available();
    }

    @Override
    public void setSwapInterval(int i) throws GLException {
        gl.setSwapInterval(i);
    }

    @Override
    public int getSwapInterval() {
        return gl.getSwapInterval();
    }

    @Override
    public Object getPlatformGLExtensions() {
        return gl.getPlatformGLExtensions();
    }

    @Override
    public Object getExtension(String s) {
        return gl.getExtension(s);
    }

    @Override
    public void glClearDepth(double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearDepth];
        gl.glClearDepth(v);
    }

    @Override
    public void glDepthRange(double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDepthRange];
        gl.glDepthRange(v, v1);
    }

    @Override
    public int getBoundBuffer(int i) {
        return gl.getBoundBuffer(i);
    }

    @Override
    public GLBufferStorage getBufferStorage(int i) {
        return gl.getBufferStorage(i);
    }

    @Override
    public GLBufferStorage mapBuffer(int i, int i1) throws GLException {
        return gl.mapBuffer(i, i1);
    }

    @Override
    public GLBufferStorage mapBufferRange(int i, long l, long l1, int i1) throws GLException {
        return gl.mapBufferRange(i, l, l1, i1);
    }

    @Override
    public boolean isVBOArrayBound() {
        return gl.isVBOArrayBound();
    }

    @Override
    public boolean isVBOElementArrayBound() {
        return gl.isVBOElementArrayBound();
    }

    @Override
    public int getBoundFramebuffer(int i) {
        return gl.getBoundFramebuffer(i);
    }

    @Override
    public int getDefaultDrawFramebuffer() {
        return gl.getDefaultDrawFramebuffer();
    }

    @Override
    public int getDefaultReadFramebuffer() {
        return gl.getDefaultReadFramebuffer();
    }

    @Override
    public int getDefaultReadBuffer() {
        return gl.getDefaultReadBuffer();
    }

    @Override
    public void glMatrixMode(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMatrixMode];
        gl.glMatrixMode(i);
    }

    @Override
    public void glPushMatrix() {
        verifyAccess();
        ++methodCalls[GLFunc.glPushMatrix];
        gl.glPushMatrix();
    }

    @Override
    public void glPopMatrix() {
        verifyAccess();
        ++methodCalls[GLFunc.glPopMatrix];
        gl.glPopMatrix();
    }

    @Override
    public void glLoadIdentity() {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadIdentity];
        gl.glLoadIdentity();
    }

    @Override
    public void glLoadMatrixf(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadMatrixf];
        gl.glLoadMatrixf(floatBuffer);
    }

    @Override
    public void glLoadMatrixf(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLoadMatrixf];
        gl.glLoadMatrixf(floats, i);
    }

    @Override
    public void glMultMatrixf(FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultMatrixf];
        gl.glMultMatrixf(floatBuffer);
    }

    @Override
    public void glMultMatrixf(float[] floats, int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultMatrixf];
        gl.glMultMatrixf(floats, i);
    }

    @Override
    public void glTranslatef(float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTranslatef];
        gl.glTranslatef(v, v1, v2);
    }

    @Override
    public void glRotatef(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glRotatef];
        gl.glRotatef(v, v1, v2, v3);
    }

    @Override
    public void glScalef(float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glScalef];
        gl.glScalef(v, v1, v2);
    }

    @Override
    public void glOrthof(float v, float v1, float v2, float v3, float v4, float v5) {
        verifyAccess();
        ++methodCalls[GLFunc.glOrthof];
        gl.glOrthof(v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glFrustumf(float v, float v1, float v2, float v3, float v4, float v5) {
        verifyAccess();
        ++methodCalls[GLFunc.glFrustumf];
        gl.glFrustumf(v, v1, v2, v3, v4, v5);
    }

    @Override
    public void glEnableClientState(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEnableClientState];
        gl.glEnableClientState(i);
    }

    @Override
    public void glDisableClientState(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDisableClientState];
        gl.glDisableClientState(i);
    }

    @Override
    public void glVertexPointer(GLArrayData glArrayData) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexPointer];
        gl.glVertexPointer(glArrayData);
    }

    @Override
    public void glVertexPointer(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexPointer];
        gl.glVertexPointer(i, i1, i2, buffer);
    }

    @Override
    public void glVertexPointer(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexPointer];
        gl.glVertexPointer(i, i1, i2, l);
    }

    @Override
    public void glColorPointer(GLArrayData glArrayData) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorPointer];
        gl.glColorPointer(glArrayData);
    }

    @Override
    public void glColorPointer(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorPointer];
        gl.glColorPointer(i, i1, i2, buffer);
    }

    @Override
    public void glColorPointer(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorPointer];
        gl.glColorPointer(i, i1, i2, l);
    }

    @Override
    public void glColor4f(float v, float v1, float v2, float v3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glColor4f];
        gl.glColor4f(v, v1, v2, v3);
    }

    @Override
    public void glNormalPointer(GLArrayData glArrayData) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormalPointer];
        gl.glNormalPointer(glArrayData);
    }

    @Override
    public void glNormalPointer(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormalPointer];
        gl.glNormalPointer(i, i1, buffer);
    }

    @Override
    public void glNormalPointer(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glNormalPointer];
        gl.glNormalPointer(i, i1, l);
    }

    @Override
    public void glTexCoordPointer(GLArrayData glArrayData) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexCoordPointer];
        gl.glTexCoordPointer(glArrayData);
    }

    @Override
    public void glTexCoordPointer(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexCoordPointer];
        gl.glTexCoordPointer(i, i1, i2, buffer);
    }

    @Override
    public void glTexCoordPointer(int i, int i1, int i2, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexCoordPointer];
        gl.glTexCoordPointer(i, i1, i2, l);
    }

    @Override
    public void glLightfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightfv];
        gl.glLightfv(i, i1, floatBuffer);
    }

    @Override
    public void glLightfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glLightfv];
        gl.glLightfv(i, i1, floats, i2);
    }

    @Override
    public void glMaterialf(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glMaterialf];
        gl.glMaterialf(i, i1, v);
    }

    @Override
    public void glMaterialfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glMaterialfv];
        gl.glMaterialfv(i, i1, floatBuffer);
    }

    @Override
    public void glMaterialfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMaterialfv];
        gl.glMaterialfv(i, i1, floats, i2);
    }

    @Override
    public void glShadeModel(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glShadeModel];
        gl.glShadeModel(i);
    }

    @Override
    public void glPolygonMode(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPolygonMode];
        gl.glPolygonMode(i, i1);
    }

    @Override
    public void glDrawBuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawBuffer];
        gl.glDrawBuffer(i);
    }

    @Override
    public void glGetDoublev(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetDoublev];
        gl.glGetDoublev(i, doubleBuffer);
    }

    @Override
    public void glGetDoublev(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetDoublev];
        gl.glGetDoublev(i, doubles, i1);
    }

    @Override
    public void glPixelStoref(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glPixelStoref];
        gl.glPixelStoref(i, v);
    }

    @Override
    public void glTexImage1D(int i, int i1, int i2, int i3, int i4, int i5, int i6, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage1D];
        gl.glTexImage1D(i, i1, i2, i3, i4, i5, i6, buffer);
    }

    @Override
    public void glTexImage1D(int i, int i1, int i2, int i3, int i4, int i5, int i6, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage1D];
        gl.glTexImage1D(i, i1, i2, i3, i4, i5, i6, l);
    }

    @Override
    public void glGetTexImage(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexImage];
        gl.glGetTexImage(i, i1, i2, i3, buffer);
    }

    @Override
    public void glGetTexImage(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexImage];
        gl.glGetTexImage(i, i1, i2, i3, l);
    }

    @Override
    public void glTexSubImage1D(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage1D];
        gl.glTexSubImage1D(i, i1, i2, i3, i4, i5, buffer);
    }

    @Override
    public void glTexSubImage1D(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage1D];
        gl.glTexSubImage1D(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glCopyTexImage1D(int i, int i1, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyTexImage1D];
        gl.glCopyTexImage1D(i, i1, i2, i3, i4, i5, i6);
    }

    @Override
    public void glCopyTexSubImage1D(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyTexSubImage1D];
        gl.glCopyTexSubImage1D(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glCompressedTexImage1D(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage1D];
        gl.glCompressedTexImage1D(i, i1, i2, i3, i4, i5, buffer);
    }

    @Override
    public void glCompressedTexImage1D(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage1D];
        gl.glCompressedTexImage1D(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glCompressedTexSubImage1D(int i, int i1, int i2, int i3, int i4, int i5, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage1D];
        gl.glCompressedTexSubImage1D(i, i1, i2, i3, i4, i5, buffer);
    }

    @Override
    public void glCompressedTexSubImage1D(int i, int i1, int i2, int i3, int i4, int i5, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage1D];
        gl.glCompressedTexSubImage1D(i, i1, i2, i3, i4, i5, l);
    }

    @Override
    public void glGetCompressedTexImage(int i, int i1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetCompressedTexImage];
        gl.glGetCompressedTexImage(i, i1, buffer);
    }

    @Override
    public void glGetCompressedTexImage(int i, int i1, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetCompressedTexImage];
        gl.glGetCompressedTexImage(i, i1, l);
    }

    @Override
    public void glMultiDrawArrays(int i, IntBuffer intBuffer, IntBuffer intBuffer1, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiDrawArrays];
        gl.glMultiDrawArrays(i, intBuffer, intBuffer1, i1);
    }

    @Override
    public void glMultiDrawArrays(int i, int[] ints, int i1, int[] ints1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiDrawArrays];
        gl.glMultiDrawArrays(i, ints, i1, ints1, i2, i3);
    }

    @Override
    public void glMultiDrawElements(int i, IntBuffer intBuffer, int i1, PointerBuffer pointerBuffer, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glMultiDrawElements];
        gl.glMultiDrawElements(i, intBuffer, i1, pointerBuffer, i2);
    }

    @Override
    public void glPointParameteri(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameteri];
        gl.glPointParameteri(i, i1);
    }

    @Override
    public void glPointParameteriv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameteriv];
        gl.glPointParameteriv(i, intBuffer);
    }

    @Override
    public void glPointParameteriv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glPointParameteriv];
        gl.glPointParameteriv(i, ints, i1);
    }

    @Override
    public void glGetBufferSubData(int i, long l, long l1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBufferSubData];
        gl.glGetBufferSubData(i, l, l1, buffer);
    }

    @Override
    public void glGetVertexAttribdv(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribdv];
        gl.glGetVertexAttribdv(i, i1, doubleBuffer);
    }

    @Override
    public void glGetVertexAttribdv(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribdv];
        gl.glGetVertexAttribdv(i, i1, doubles, i2);
    }

    @Override
    public void glVertexAttrib1d(int i, double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1d];
        gl.glVertexAttrib1d(i, v);
    }

    @Override
    public void glVertexAttrib1dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1dv];
        gl.glVertexAttrib1dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib1dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1dv];
        gl.glVertexAttrib1dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib1s(int i, short i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1s];
        gl.glVertexAttrib1s(i, i1);
    }

    @Override
    public void glVertexAttrib1sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1sv];
        gl.glVertexAttrib1sv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib1sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1sv];
        gl.glVertexAttrib1sv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib2d(int i, double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2d];
        gl.glVertexAttrib2d(i, v, v1);
    }

    @Override
    public void glVertexAttrib2dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2dv];
        gl.glVertexAttrib2dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib2dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2dv];
        gl.glVertexAttrib2dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib2s(int i, short i1, short i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2s];
        gl.glVertexAttrib2s(i, i1, i2);
    }

    @Override
    public void glVertexAttrib2sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2sv];
        gl.glVertexAttrib2sv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib2sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2sv];
        gl.glVertexAttrib2sv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib3d(int i, double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3d];
        gl.glVertexAttrib3d(i, v, v1, v2);
    }

    @Override
    public void glVertexAttrib3dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3dv];
        gl.glVertexAttrib3dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib3dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3dv];
        gl.glVertexAttrib3dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib3s(int i, short i1, short i2, short i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3s];
        gl.glVertexAttrib3s(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttrib3sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3sv];
        gl.glVertexAttrib3sv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib3sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3sv];
        gl.glVertexAttrib3sv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4Nbv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nbv];
        gl.glVertexAttrib4Nbv(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4Nbv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nbv];
        gl.glVertexAttrib4Nbv(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4Niv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Niv];
        gl.glVertexAttrib4Niv(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4Niv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Niv];
        gl.glVertexAttrib4Niv(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4Nsv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nsv];
        gl.glVertexAttrib4Nsv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4Nsv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nsv];
        gl.glVertexAttrib4Nsv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4Nub(int i, byte b, byte b1, byte b2, byte b3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nub];
        gl.glVertexAttrib4Nub(i, b, b1, b2, b3);
    }

    @Override
    public void glVertexAttrib4Nubv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nubv];
        gl.glVertexAttrib4Nubv(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4Nubv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nubv];
        gl.glVertexAttrib4Nubv(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4Nuiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nuiv];
        gl.glVertexAttrib4Nuiv(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4Nuiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nuiv];
        gl.glVertexAttrib4Nuiv(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4Nusv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nusv];
        gl.glVertexAttrib4Nusv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4Nusv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4Nusv];
        gl.glVertexAttrib4Nusv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4bv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4bv];
        gl.glVertexAttrib4bv(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4bv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4bv];
        gl.glVertexAttrib4bv(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4d(int i, double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4d];
        gl.glVertexAttrib4d(i, v, v1, v2, v3);
    }

    @Override
    public void glVertexAttrib4dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4dv];
        gl.glVertexAttrib4dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttrib4dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4dv];
        gl.glVertexAttrib4dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttrib4iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4iv];
        gl.glVertexAttrib4iv(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4iv];
        gl.glVertexAttrib4iv(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4s(int i, short i1, short i2, short i3, short i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4s];
        gl.glVertexAttrib4s(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttrib4sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4sv];
        gl.glVertexAttrib4sv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4sv];
        gl.glVertexAttrib4sv(i, shorts, i1);
    }

    @Override
    public void glVertexAttrib4ubv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4ubv];
        gl.glVertexAttrib4ubv(i, byteBuffer);
    }

    @Override
    public void glVertexAttrib4ubv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4ubv];
        gl.glVertexAttrib4ubv(i, bytes, i1);
    }

    @Override
    public void glVertexAttrib4uiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4uiv];
        gl.glVertexAttrib4uiv(i, intBuffer);
    }

    @Override
    public void glVertexAttrib4uiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4uiv];
        gl.glVertexAttrib4uiv(i, ints, i1);
    }

    @Override
    public void glVertexAttrib4usv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4usv];
        gl.glVertexAttrib4usv(i, shortBuffer);
    }

    @Override
    public void glVertexAttrib4usv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4usv];
        gl.glVertexAttrib4usv(i, shorts, i1);
    }

    @Override
    public void glClampColor(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glClampColor];
        gl.glClampColor(i, i1);
    }

    @Override
    public void glVertexAttribI1i(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1i];
        gl.glVertexAttribI1i(i, i1);
    }

    @Override
    public void glVertexAttribI2i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2i];
        gl.glVertexAttribI2i(i, i1, i2);
    }

    @Override
    public void glVertexAttribI3i(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3i];
        gl.glVertexAttribI3i(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttribI1ui(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1ui];
        gl.glVertexAttribI1ui(i, i1);
    }

    @Override
    public void glVertexAttribI2ui(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2ui];
        gl.glVertexAttribI2ui(i, i1, i2);
    }

    @Override
    public void glVertexAttribI3ui(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3ui];
        gl.glVertexAttribI3ui(i, i1, i2, i3);
    }

    @Override
    public void glVertexAttribI1iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1iv];
        gl.glVertexAttribI1iv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI1iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1iv];
        gl.glVertexAttribI1iv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI2iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2iv];
        gl.glVertexAttribI2iv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI2iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2iv];
        gl.glVertexAttribI2iv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI3iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3iv];
        gl.glVertexAttribI3iv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI3iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3iv];
        gl.glVertexAttribI3iv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI1uiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1uiv];
        gl.glVertexAttribI1uiv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI1uiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI1uiv];
        gl.glVertexAttribI1uiv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI2uiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2uiv];
        gl.glVertexAttribI2uiv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI2uiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI2uiv];
        gl.glVertexAttribI2uiv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI3uiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3uiv];
        gl.glVertexAttribI3uiv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI3uiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI3uiv];
        gl.glVertexAttribI3uiv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI4bv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4bv];
        gl.glVertexAttribI4bv(i, byteBuffer);
    }

    @Override
    public void glVertexAttribI4bv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4bv];
        gl.glVertexAttribI4bv(i, bytes, i1);
    }

    @Override
    public void glVertexAttribI4sv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4sv];
        gl.glVertexAttribI4sv(i, shortBuffer);
    }

    @Override
    public void glVertexAttribI4sv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4sv];
        gl.glVertexAttribI4sv(i, shorts, i1);
    }

    @Override
    public void glVertexAttribI4ubv(int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4ubv];
        gl.glVertexAttribI4ubv(i, byteBuffer);
    }

    @Override
    public void glVertexAttribI4ubv(int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4ubv];
        gl.glVertexAttribI4ubv(i, bytes, i1);
    }

    @Override
    public void glVertexAttribI4usv(int i, ShortBuffer shortBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4usv];
        gl.glVertexAttribI4usv(i, shortBuffer);
    }

    @Override
    public void glVertexAttribI4usv(int i, short[] shorts, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4usv];
        gl.glVertexAttribI4usv(i, shorts, i1);
    }

    @Override
    public void glBindFragDataLocation(int i, int i1, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindFragDataLocation];
        gl.glBindFragDataLocation(i, i1, s);
    }

    @Override
    public void glFramebufferTexture1D(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferTexture1D];
        gl.glFramebufferTexture1D(i, i1, i2, i3, i4);
    }

    @Override
    public void glPrimitiveRestartIndex(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glPrimitiveRestartIndex];
        gl.glPrimitiveRestartIndex(i);
    }

    @Override
    public void glGetActiveUniformName(int i, int i1, int i2, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformName];
        gl.glGetActiveUniformName(i, i1, i2, intBuffer, byteBuffer);
    }

    @Override
    public void glGetActiveUniformName(int i, int i1, int i2, int[] ints, int i3, byte[] bytes, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformName];
        gl.glGetActiveUniformName(i, i1, i2, ints, i3, bytes, i4);
    }

    @Override
    public void glProvokingVertex(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glProvokingVertex];
        gl.glProvokingVertex(i);
    }

    @Override
    public void glDrawTransformFeedback(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawTransformFeedback];
        gl.glDrawTransformFeedback(i, i1);
    }

    @Override
    public void glDrawTransformFeedbackStream(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawTransformFeedbackStream];
        gl.glDrawTransformFeedbackStream(i, i1, i2);
    }

    @Override
    public void glBeginQueryIndexed(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBeginQueryIndexed];
        gl.glBeginQueryIndexed(i, i1, i2);
    }

    @Override
    public void glEndQueryIndexed(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEndQueryIndexed];
        gl.glEndQueryIndexed(i, i1);
    }

    @Override
    public void glGetQueryIndexediv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryIndexediv];
        gl.glGetQueryIndexediv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetQueryIndexediv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryIndexediv];
        gl.glGetQueryIndexediv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform1d(int i, int i1, double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1d];
        gl.glProgramUniform1d(i, i1, v);
    }

    @Override
    public void glProgramUniform1dv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1dv];
        gl.glProgramUniform1dv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform1dv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1dv];
        gl.glProgramUniform1dv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform2d(int i, int i1, double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2d];
        gl.glProgramUniform2d(i, i1, v, v1);
    }

    @Override
    public void glProgramUniform2dv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2dv];
        gl.glProgramUniform2dv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform2dv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2dv];
        gl.glProgramUniform2dv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform3d(int i, int i1, double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3d];
        gl.glProgramUniform3d(i, i1, v, v1, v2);
    }

    @Override
    public void glProgramUniform3dv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3dv];
        gl.glProgramUniform3dv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform3dv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3dv];
        gl.glProgramUniform3dv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniform4d(int i, int i1, double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4d];
        gl.glProgramUniform4d(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4dv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4dv];
        gl.glProgramUniform4dv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glProgramUniform4dv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4dv];
        gl.glProgramUniform4dv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2dv];
        gl.glProgramUniformMatrix2dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2dv];
        gl.glProgramUniformMatrix2dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3dv];
        gl.glProgramUniformMatrix3dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3dv];
        gl.glProgramUniformMatrix3dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4dv];
        gl.glProgramUniformMatrix4dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4dv];
        gl.glProgramUniformMatrix4dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2x3dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x3dv];
        gl.glProgramUniformMatrix2x3dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x3dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x3dv];
        gl.glProgramUniformMatrix2x3dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3x2dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x2dv];
        gl.glProgramUniformMatrix3x2dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x2dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x2dv];
        gl.glProgramUniformMatrix3x2dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix2x4dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x4dv];
        gl.glProgramUniformMatrix2x4dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x4dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x4dv];
        gl.glProgramUniformMatrix2x4dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4x2dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x2dv];
        gl.glProgramUniformMatrix4x2dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x2dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x2dv];
        gl.glProgramUniformMatrix4x2dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix3x4dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x4dv];
        gl.glProgramUniformMatrix3x4dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x4dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x4dv];
        gl.glProgramUniformMatrix3x4dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glProgramUniformMatrix4x3dv(int i, int i1, int i2, boolean b, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x3dv];
        gl.glProgramUniformMatrix4x3dv(i, i1, i2, b, doubleBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x3dv(int i, int i1, int i2, boolean b, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x3dv];
        gl.glProgramUniformMatrix4x3dv(i, i1, i2, b, doubles, i3);
    }

    @Override
    public void glVertexAttribL1d(int i, double v) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL1d];
        gl.glVertexAttribL1d(i, v);
    }

    @Override
    public void glVertexAttribL2d(int i, double v, double v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL2d];
        gl.glVertexAttribL2d(i, v, v1);
    }

    @Override
    public void glVertexAttribL3d(int i, double v, double v1, double v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL3d];
        gl.glVertexAttribL3d(i, v, v1, v2);
    }

    @Override
    public void glVertexAttribL4d(int i, double v, double v1, double v2, double v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL4d];
        gl.glVertexAttribL4d(i, v, v1, v2, v3);
    }

    @Override
    public void glVertexAttribL1dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL1dv];
        gl.glVertexAttribL1dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttribL1dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL1dv];
        gl.glVertexAttribL1dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttribL2dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL2dv];
        gl.glVertexAttribL2dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttribL2dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL2dv];
        gl.glVertexAttribL2dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttribL3dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL3dv];
        gl.glVertexAttribL3dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttribL3dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL3dv];
        gl.glVertexAttribL3dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttribL4dv(int i, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL4dv];
        gl.glVertexAttribL4dv(i, doubleBuffer);
    }

    @Override
    public void glVertexAttribL4dv(int i, double[] doubles, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribL4dv];
        gl.glVertexAttribL4dv(i, doubles, i1);
    }

    @Override
    public void glVertexAttribLPointer(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribLPointer];
        gl.glVertexAttribLPointer(i, i1, i2, i3, l);
    }

    @Override
    public void glGetVertexAttribLdv(int i, int i1, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribLdv];
        gl.glGetVertexAttribLdv(i, i1, doubleBuffer);
    }

    @Override
    public void glGetVertexAttribLdv(int i, int i1, double[] doubles, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribLdv];
        gl.glGetVertexAttribLdv(i, i1, doubles, i2);
    }

    @Override
    public void glGetActiveAtomicCounterBufferiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveAtomicCounterBufferiv];
        gl.glGetActiveAtomicCounterBufferiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetActiveAtomicCounterBufferiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveAtomicCounterBufferiv];
        gl.glGetActiveAtomicCounterBufferiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glClearBufferData(int i, int i1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferData];
        gl.glClearBufferData(i, i1, i2, i3, buffer);
    }

    @Override
    public void glClearBufferSubData(int i, int i1, long l, long l1, int i2, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferSubData];
        gl.glClearBufferSubData(i, i1, l, l1, i2, i3, buffer);
    }

    @Override
    public void glGetInternalformati64v(int i, int i1, int i2, int i3, LongBuffer longBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetInternalformati64v];
        gl.glGetInternalformati64v(i, i1, i2, i3, longBuffer);
    }

    @Override
    public void glGetInternalformati64v(int i, int i1, int i2, int i3, long[] longs, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetInternalformati64v];
        gl.glGetInternalformati64v(i, i1, i2, i3, longs, i4);
    }

    @Override
    public void glInvalidateTexSubImage(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateTexSubImage];
        gl.glInvalidateTexSubImage(i, i1, i2, i3, i4, i5, i6, i7);
    }

    @Override
    public void glInvalidateTexImage(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateTexImage];
        gl.glInvalidateTexImage(i, i1);
    }

    @Override
    public void glInvalidateBufferSubData(int i, long l, long l1) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateBufferSubData];
        gl.glInvalidateBufferSubData(i, l, l1);
    }

    @Override
    public void glInvalidateBufferData(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateBufferData];
        gl.glInvalidateBufferData(i);
    }

    @Override
    public void glGetnCompressedTexImage(int i, int i1, int i2, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnCompressedTexImage];
        gl.glGetnCompressedTexImage(i, i1, i2, buffer);
    }

    @Override
    public void glGetnTexImage(int i, int i1, int i2, int i3, int i4, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnTexImage];
        gl.glGetnTexImage(i, i1, i2, i3, i4, buffer);
    }

    @Override
    public void glGetnUniformdv(int i, int i1, int i2, DoubleBuffer doubleBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformdv];
        gl.glGetnUniformdv(i, i1, i2, doubleBuffer);
    }

    @Override
    public void glGetnUniformdv(int i, int i1, int i2, double[] doubles, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformdv];
        gl.glGetnUniformdv(i, i1, i2, doubles, i3);
    }

    @Override
    public void glBufferPageCommitmentARB(int i, long l, long l1, boolean b) {
        verifyAccess();
        gl.glBufferPageCommitmentARB(i, l, l1, b);
    }

    @Override
    public void glNamedBufferPageCommitmentEXT(int i, long l, long l1, boolean b) {
        verifyAccess();
        gl.glNamedBufferPageCommitmentEXT(i, l, l1, b);
    }

    @Override
    public void glNamedBufferPageCommitmentARB(int i, long l, long l1, boolean b) {
        verifyAccess();
        gl.glNamedBufferPageCommitmentARB(i, l, l1, b);
    }

    @Override
    public void glTexPageCommitmentARB(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, boolean b) {
        verifyAccess();
        gl.glTexPageCommitmentARB(i, i1, i2, i3, i4, i5, i6, i7, b);
    }

    @Override
    public void glDebugMessageEnableAMD(int i, int i1, int i2, IntBuffer intBuffer, boolean b) {
        verifyAccess();
        gl.glDebugMessageEnableAMD(i, i1, i2, intBuffer, b);
    }

    @Override
    public void glDebugMessageEnableAMD(int i, int i1, int i2, int[] ints, int i3, boolean b) {
        verifyAccess();
        gl.glDebugMessageEnableAMD(i, i1, i2, ints, i3, b);
    }

    @Override
    public void glDebugMessageInsertAMD(int i, int i1, int i2, int i3, String s) {
        verifyAccess();
        gl.glDebugMessageInsertAMD(i, i1, i2, i3, s);
    }

    @Override
    public int glGetDebugMessageLogAMD(int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, IntBuffer intBuffer3, ByteBuffer byteBuffer) {
        verifyAccess();
        return gl.glGetDebugMessageLogAMD(i, i1, intBuffer, intBuffer1, intBuffer2, intBuffer3, byteBuffer);
    }

    @Override
    public int glGetDebugMessageLogAMD(int i, int i1, int[] ints, int i2, int[] ints1, int i3, int[] ints2, int i4, int[] ints3, int i5, byte[] bytes, int i6) {
        verifyAccess();
        return gl.glGetDebugMessageLogAMD(i, i1, ints, i2, ints1, i3, ints2, i4, ints3, i5, bytes, i6);
    }

    @Override
    public void glGetUniformui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetUniformui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetUniformui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetUniformui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glMultiDrawArraysIndirectAMD(int i, Buffer buffer, int i1, int i2) {
        verifyAccess();
        gl.glMultiDrawArraysIndirectAMD(i, buffer, i1, i2);
    }

    @Override
    public void glMultiDrawElementsIndirectAMD(int i, int i1, Buffer buffer, int i2, int i3) {
        verifyAccess();
        gl.glMultiDrawElementsIndirectAMD(i, i1, buffer, i2, i3);
    }

    @Override
    public void glSetMultisamplefvAMD(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        gl.glSetMultisamplefvAMD(i, i1, floatBuffer);
    }

    @Override
    public void glSetMultisamplefvAMD(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        gl.glSetMultisamplefvAMD(i, i1, floats, i2);
    }

    @Override
    public void glStencilOpValueAMD(int i, int i1) {
        verifyAccess();
        gl.glStencilOpValueAMD(i, i1);
    }

    @Override
    public void glTessellationFactorAMD(float v) {
        verifyAccess();
        gl.glTessellationFactorAMD(v);
    }

    @Override
    public void glTessellationModeAMD(int i) {
        verifyAccess();
        gl.glTessellationModeAMD(i);
    }

    @Override
    public long glImportSyncEXT(int i, long l, int i1) {
        verifyAccess();
        return gl.glImportSyncEXT(i, l, i1);
    }

    @Override
    public void glMakeBufferResidentNV(int i, int i1) {
        verifyAccess();
        gl.glMakeBufferResidentNV(i, i1);
    }

    @Override
    public void glMakeBufferNonResidentNV(int i) {
        verifyAccess();
        gl.glMakeBufferNonResidentNV(i);
    }

    @Override
    public boolean glIsBufferResidentNV(int i) {
        verifyAccess();
        return gl.glIsBufferResidentNV(i);
    }

    @Override
    public void glMakeNamedBufferResidentNV(int i, int i1) {
        verifyAccess();
        gl.glMakeNamedBufferResidentNV(i, i1);
    }

    @Override
    public void glMakeNamedBufferNonResidentNV(int i) {
        verifyAccess();
        gl.glMakeNamedBufferNonResidentNV(i);
    }

    @Override
    public boolean glIsNamedBufferResidentNV(int i) {
        verifyAccess();
        return gl.glIsNamedBufferResidentNV(i);
    }

    @Override
    public void glGetBufferParameterui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetBufferParameterui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetBufferParameterui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetBufferParameterui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetNamedBufferParameterui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetNamedBufferParameterui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetNamedBufferParameterui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glGetIntegerui64vNV(int i, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetIntegerui64vNV(i, longBuffer);
    }

    @Override
    public void glGetIntegerui64vNV(int i, long[] longs, int i1) {
        verifyAccess();
        gl.glGetIntegerui64vNV(i, longs, i1);
    }

    @Override
    public void glUniformui64NV(int i, long l) {
        verifyAccess();
        gl.glUniformui64NV(i, l);
    }

    @Override
    public void glUniformui64vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glUniformui64vNV(i, i1, longBuffer);
    }

    @Override
    public void glUniformui64vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glUniformui64vNV(i, i1, longs, i2);
    }

    @Override
    public void glProgramUniformui64NV(int i, int i1, long l) {
        verifyAccess();
        gl.glProgramUniformui64NV(i, i1, l);
    }

    @Override
    public void glProgramUniformui64vNV(int i, int i1, int i2, LongBuffer longBuffer) {
        verifyAccess();
        gl.glProgramUniformui64vNV(i, i1, i2, longBuffer);
    }

    @Override
    public void glProgramUniformui64vNV(int i, int i1, int i2, long[] longs, int i3) {
        verifyAccess();
        gl.glProgramUniformui64vNV(i, i1, i2, longs, i3);
    }

    @Override
    public void glTexImage2DMultisampleCoverageNV(int i, int i1, int i2, int i3, int i4, int i5, boolean b) {
        verifyAccess();
        gl.glTexImage2DMultisampleCoverageNV(i, i1, i2, i3, i4, i5, b);
    }

    @Override
    public void glTexImage3DMultisampleCoverageNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b) {
        verifyAccess();
        gl.glTexImage3DMultisampleCoverageNV(i, i1, i2, i3, i4, i5, i6, b);
    }

    @Override
    public void glTextureImage2DMultisampleNV(int i, int i1, int i2, int i3, int i4, int i5, boolean b) {
        verifyAccess();
        gl.glTextureImage2DMultisampleNV(i, i1, i2, i3, i4, i5, b);
    }

    @Override
    public void glTextureImage3DMultisampleNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b) {
        verifyAccess();
        gl.glTextureImage3DMultisampleNV(i, i1, i2, i3, i4, i5, i6, b);
    }

    @Override
    public void glTextureImage2DMultisampleCoverageNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, boolean b) {
        verifyAccess();
        gl.glTextureImage2DMultisampleCoverageNV(i, i1, i2, i3, i4, i5, i6, b);
    }

    @Override
    public void glTextureImage3DMultisampleCoverageNV(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, boolean b) {
        verifyAccess();
        gl.glTextureImage3DMultisampleCoverageNV(i, i1, i2, i3, i4, i5, i6, i7, b);
    }

    @Override
    public void glBufferAddressRangeNV(int i, int i1, long l, long l1) {
        verifyAccess();
        gl.glBufferAddressRangeNV(i, i1, l, l1);
    }

    @Override
    public void glVertexFormatNV(int i, int i1, int i2) {
        verifyAccess();
        gl.glVertexFormatNV(i, i1, i2);
    }

    @Override
    public void glNormalFormatNV(int i, int i1) {
        verifyAccess();
        gl.glNormalFormatNV(i, i1);
    }

    @Override
    public void glColorFormatNV(int i, int i1, int i2) {
        verifyAccess();
        gl.glColorFormatNV(i, i1, i2);
    }

    @Override
    public void glIndexFormatNV(int i, int i1) {
        verifyAccess();
        gl.glIndexFormatNV(i, i1);
    }

    @Override
    public void glTexCoordFormatNV(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glTexCoordFormatNV(i, i1, i2);
    }

    @Override
    public void glEdgeFlagFormatNV(int i) {
        verifyAccess();
        gl.glEdgeFlagFormatNV(i);
    }

    @Override
    public void glSecondaryColorFormatNV(int i, int i1, int i2) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glSecondaryColorFormatNV(i, i1, i2);
    }

    @Override
    public void glFogCoordFormatNV(int i, int i1) {
        verifyAccess();
        if (drawingDisabled) return;
        gl.glFogCoordFormatNV(i, i1);
    }

    @Override
    public void glVertexAttribFormatNV(int i, int i1, int i2, boolean b, int i3) {
        verifyAccess();
        gl.glVertexAttribFormatNV(i, i1, i2, b, i3);
    }

    @Override
    public void glVertexAttribIFormatNV(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glVertexAttribIFormatNV(i, i1, i2, i3);
    }

    @Override
    public void glGetIntegerui64i_vNV(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        gl.glGetIntegerui64i_vNV(i, i1, longBuffer);
    }

    @Override
    public void glGetIntegerui64i_vNV(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        gl.glGetIntegerui64i_vNV(i, i1, longs, i2);
    }

    @Override
    public void glReadBuffer(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glReadBuffer];
        gl.glReadBuffer(i);
    }

    @Override
    public void glGetTexLevelParameterfv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexLevelParameterfv];
        gl.glGetTexLevelParameterfv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glGetTexLevelParameterfv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexLevelParameterfv];
        gl.glGetTexLevelParameterfv(i, i1, i2, floats, i3);
    }

    @Override
    public void glGetTexLevelParameteriv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexLevelParameteriv];
        gl.glGetTexLevelParameteriv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetTexLevelParameteriv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexLevelParameteriv];
        gl.glGetTexLevelParameteriv(i, i1, i2, ints, i3);
    }

    @Override
    public void glDrawRangeElements(int i, int i1, int i2, int i3, int i4, long l) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawRangeElements];
        gl.glDrawRangeElements(i, i1, i2, i3, i4, l);
    }

    @Override
    public void glUniformMatrix2x3fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2x3fv];
        gl.glUniformMatrix2x3fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix2x3fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2x3fv];
        gl.glUniformMatrix2x3fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix3x2fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3x2fv];
        gl.glUniformMatrix3x2fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix3x2fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3x2fv];
        gl.glUniformMatrix3x2fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix2x4fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2x4fv];
        gl.glUniformMatrix2x4fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix2x4fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2x4fv];
        gl.glUniformMatrix2x4fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix4x2fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4x2fv];
        gl.glUniformMatrix4x2fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix4x2fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4x2fv];
        gl.glUniformMatrix4x2fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix3x4fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3x4fv];
        gl.glUniformMatrix3x4fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix3x4fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3x4fv];
        gl.glUniformMatrix3x4fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix4x3fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4x3fv];
        gl.glUniformMatrix4x3fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix4x3fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4x3fv];
        gl.glUniformMatrix4x3fv(i, i1, b, floats, i2);
    }

    @Override
    public void glColorMaski(int i, boolean b, boolean b1, boolean b2, boolean b3) {
        verifyAccess();
        ++methodCalls[GLFunc.glColorMaski];
        gl.glColorMaski(i, b, b1, b2, b3);
    }

    @Override
    public void glGetBooleani_v(int i, int i1, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleani_v];
        gl.glGetBooleani_v(i, i1, byteBuffer);
    }

    @Override
    public void glGetBooleani_v(int i, int i1, byte[] bytes, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetBooleani_v];
        gl.glGetBooleani_v(i, i1, bytes, i2);
    }

    @Override
    public void glGetIntegeri_v(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegeri_v];
        gl.glGetIntegeri_v(i, i1, intBuffer);
    }

    @Override
    public void glGetIntegeri_v(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetIntegeri_v];
        gl.glGetIntegeri_v(i, i1, ints, i2);
    }

    @Override
    public void glEnablei(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glEnablei];
        gl.glEnablei(i, i1);
    }

    @Override
    public void glDisablei(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDisablei];
        gl.glDisablei(i, i1);
    }

    @Override
    public boolean glIsEnabledi(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsEnabledi];
        return gl.glIsEnabledi(i, i1);
    }

    @Override
    public void glBeginTransformFeedback(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glBeginTransformFeedback];
        gl.glBeginTransformFeedback(i);
    }

    @Override
    public void glEndTransformFeedback() {
        verifyAccess();
        ++methodCalls[GLFunc.glEndTransformFeedback];
        gl.glEndTransformFeedback();
    }

    @Override
    public void glBindBufferRange(int i, int i1, int i2, long l, long l1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindBufferRange];
        gl.glBindBufferRange(i, i1, i2, l, l1);
    }

    @Override
    public void glBindBufferBase(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindBufferBase];
        gl.glBindBufferBase(i, i1, i2);
    }

    @Override
    public void glTransformFeedbackVaryings(int i, int i1, String[] strings, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTransformFeedbackVaryings];
        gl.glTransformFeedbackVaryings(i, i1, strings, i2);
    }

    @Override
    public void glGetTransformFeedbackVarying(int i, int i1, int i2, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTransformFeedbackVarying];
        gl.glGetTransformFeedbackVarying(i, i1, i2, intBuffer, intBuffer1, intBuffer2, byteBuffer);
    }

    @Override
    public void glGetTransformFeedbackVarying(int i, int i1, int i2, int[] ints, int i3, int[] ints1, int i4, int[] ints2, int i5, byte[] bytes, int i6) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTransformFeedbackVarying];
        gl.glGetTransformFeedbackVarying(i, i1, i2, ints, i3, ints1, i4, ints2, i5, bytes, i6);
    }

    @Override
    public void glBeginConditionalRender(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBeginConditionalRender];
        gl.glBeginConditionalRender(i, i1);
    }

    @Override
    public void glEndConditionalRender() {
        verifyAccess();
        ++methodCalls[GLFunc.glEndConditionalRender];
        gl.glEndConditionalRender();
    }

    @Override
    public void glVertexAttribIPointer(int i, int i1, int i2, int i3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribIPointer];
        gl.glVertexAttribIPointer(i, i1, i2, i3, l);
    }

    @Override
    public void glGetVertexAttribIiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribIiv];
        gl.glGetVertexAttribIiv(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribIiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribIiv];
        gl.glGetVertexAttribIiv(i, i1, ints, i2);
    }

    @Override
    public void glGetVertexAttribIuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribIuiv];
        gl.glGetVertexAttribIuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribIuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribIuiv];
        gl.glGetVertexAttribIuiv(i, i1, ints, i2);
    }

    @Override
    public void glVertexAttribI4i(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4i];
        gl.glVertexAttribI4i(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttribI4ui(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4ui];
        gl.glVertexAttribI4ui(i, i1, i2, i3, i4);
    }

    @Override
    public void glVertexAttribI4iv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4iv];
        gl.glVertexAttribI4iv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI4iv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4iv];
        gl.glVertexAttribI4iv(i, ints, i1);
    }

    @Override
    public void glVertexAttribI4uiv(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4uiv];
        gl.glVertexAttribI4uiv(i, intBuffer);
    }

    @Override
    public void glVertexAttribI4uiv(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribI4uiv];
        gl.glVertexAttribI4uiv(i, ints, i1);
    }

    @Override
    public void glGetUniformuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformuiv];
        gl.glGetUniformuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetUniformuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformuiv];
        gl.glGetUniformuiv(i, i1, ints, i2);
    }

    @Override
    public int glGetFragDataLocation(int i, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFragDataLocation];
        return gl.glGetFragDataLocation(i, s);
    }

    @Override
    public void glUniform1ui(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1ui];
        gl.glUniform1ui(i, i1);
    }

    @Override
    public void glUniform2ui(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2ui];
        gl.glUniform2ui(i, i1, i2);
    }

    @Override
    public void glUniform3ui(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3ui];
        gl.glUniform3ui(i, i1, i2, i3);
    }

    @Override
    public void glUniform4ui(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4ui];
        gl.glUniform4ui(i, i1, i2, i3, i4);
    }

    @Override
    public void glUniform1uiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1uiv];
        gl.glUniform1uiv(i, i1, intBuffer);
    }

    @Override
    public void glUniform1uiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1uiv];
        gl.glUniform1uiv(i, i1, ints, i2);
    }

    @Override
    public void glUniform2uiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2uiv];
        gl.glUniform2uiv(i, i1, intBuffer);
    }

    @Override
    public void glUniform2uiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2uiv];
        gl.glUniform2uiv(i, i1, ints, i2);
    }

    @Override
    public void glUniform3uiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3uiv];
        gl.glUniform3uiv(i, i1, intBuffer);
    }

    @Override
    public void glUniform3uiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3uiv];
        gl.glUniform3uiv(i, i1, ints, i2);
    }

    @Override
    public void glUniform4uiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4uiv];
        gl.glUniform4uiv(i, i1, intBuffer);
    }

    @Override
    public void glUniform4uiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4uiv];
        gl.glUniform4uiv(i, i1, ints, i2);
    }

    @Override
    public void glClearBufferiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferiv];
        gl.glClearBufferiv(i, i1, intBuffer);
    }

    @Override
    public void glClearBufferiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferiv];
        gl.glClearBufferiv(i, i1, ints, i2);
    }

    @Override
    public void glClearBufferuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferuiv];
        gl.glClearBufferuiv(i, i1, intBuffer);
    }

    @Override
    public void glClearBufferuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferuiv];
        gl.glClearBufferuiv(i, i1, ints, i2);
    }

    @Override
    public void glClearBufferfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferfv];
        gl.glClearBufferfv(i, i1, floatBuffer);
    }

    @Override
    public void glClearBufferfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferfv];
        gl.glClearBufferfv(i, i1, floats, i2);
    }

    @Override
    public void glClearBufferfi(int i, int i1, float v, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glClearBufferfi];
        gl.glClearBufferfi(i, i1, v, i2);
    }

    @Override
    public String glGetStringi(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetStringi];
        return gl.glGetStringi(i, i1);
    }

    @Override
    public void glBlitFramebuffer(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlitFramebuffer];
        gl.glBlitFramebuffer(i, i1, i2, i3, i4, i5, i6, i7, i8, i9);
    }

    @Override
    public void glFramebufferTextureLayer(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferTextureLayer];
        gl.glFramebufferTextureLayer(i, i1, i2, i3, i4);
    }

    @Override
    public void glBindVertexArray(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindVertexArray];
        gl.glBindVertexArray(i);
    }

    @Override
    public void glDeleteVertexArrays(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteVertexArrays];
        gl.glDeleteVertexArrays(i, intBuffer);
    }

    @Override
    public void glDeleteVertexArrays(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteVertexArrays];
        gl.glDeleteVertexArrays(i, ints, i1);
    }

    @Override
    public void glGenVertexArrays(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenVertexArrays];
        gl.glGenVertexArrays(i, intBuffer);
    }

    @Override
    public void glGenVertexArrays(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenVertexArrays];
        gl.glGenVertexArrays(i, ints, i1);
    }

    @Override
    public boolean glIsVertexArray(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsVertexArray];
        return gl.glIsVertexArray(i);
    }

    @Override
    public void glDrawArraysInstanced(int i, int i1, int i2, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawArraysInstanced];
        gl.glDrawArraysInstanced(i, i1, i2, i3);
    }

    @Override
    public void glDrawElementsInstanced(int i, int i1, int i2, long l, int i3) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElementsInstanced];
        gl.glDrawElementsInstanced(i, i1, i2, l, i3);
    }

    @Override
    public void glTexBuffer(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexBuffer];
        gl.glTexBuffer(i, i1, i2);
    }

    @Override
    public void glCopyBufferSubData(int i, int i1, long l, long l1, long l2) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyBufferSubData];
        gl.glCopyBufferSubData(i, i1, l, l1, l2);
    }

    @Override
    public void glGetUniformIndices(int i, int i1, String[] strings, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformIndices];
        gl.glGetUniformIndices(i, i1, strings, intBuffer);
    }

    @Override
    public void glGetUniformIndices(int i, int i1, String[] strings, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformIndices];
        gl.glGetUniformIndices(i, i1, strings, ints, i2);
    }

    @Override
    public void glGetActiveUniformsiv(int i, int i1, IntBuffer intBuffer, int i2, IntBuffer intBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformsiv];
        gl.glGetActiveUniformsiv(i, i1, intBuffer, i2, intBuffer1);
    }

    @Override
    public void glGetActiveUniformsiv(int i, int i1, int[] ints, int i2, int i3, int[] ints1, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformsiv];
        gl.glGetActiveUniformsiv(i, i1, ints, i2, i3, ints1, i4);
    }

    @Override
    public int glGetUniformBlockIndex(int i, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformBlockIndex];
        return gl.glGetUniformBlockIndex(i, s);
    }

    @Override
    public void glGetActiveUniformBlockiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformBlockiv];
        gl.glGetActiveUniformBlockiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetActiveUniformBlockiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformBlockiv];
        gl.glGetActiveUniformBlockiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glGetActiveUniformBlockName(int i, int i1, int i2, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformBlockName];
        gl.glGetActiveUniformBlockName(i, i1, i2, intBuffer, byteBuffer);
    }

    @Override
    public void glGetActiveUniformBlockName(int i, int i1, int i2, int[] ints, int i3, byte[] bytes, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniformBlockName];
        gl.glGetActiveUniformBlockName(i, i1, i2, ints, i3, bytes, i4);
    }

    @Override
    public void glUniformBlockBinding(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformBlockBinding];
        gl.glUniformBlockBinding(i, i1, i2);
    }

    @Override
    public void glVertexAttribDivisor(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribDivisor];
        gl.glVertexAttribDivisor(i, i1);
    }

    @Override
    public void glMinSampleShading(float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glMinSampleShading];
        gl.glMinSampleShading(v);
    }

    @Override
    public void glBlendEquationi(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendEquationi];
        gl.glBlendEquationi(i, i1);
    }

    @Override
    public void glBlendEquationSeparatei(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendEquationSeparatei];
        gl.glBlendEquationSeparatei(i, i1, i2);
    }

    @Override
    public void glBlendFunci(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendFunci];
        gl.glBlendFunci(i, i1, i2);
    }

    @Override
    public void glBlendFuncSeparatei(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendFuncSeparatei];
        gl.glBlendFuncSeparatei(i, i1, i2, i3, i4);
    }

    @Override
    public void glBindTransformFeedback(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindTransformFeedback];
        gl.glBindTransformFeedback(i, i1);
    }

    @Override
    public void glDeleteTransformFeedbacks(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteTransformFeedbacks];
        gl.glDeleteTransformFeedbacks(i, intBuffer);
    }

    @Override
    public void glDeleteTransformFeedbacks(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteTransformFeedbacks];
        gl.glDeleteTransformFeedbacks(i, ints, i1);
    }

    @Override
    public void glGenTransformFeedbacks(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenTransformFeedbacks];
        gl.glGenTransformFeedbacks(i, intBuffer);
    }

    @Override
    public void glGenTransformFeedbacks(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenTransformFeedbacks];
        gl.glGenTransformFeedbacks(i, ints, i1);
    }

    @Override
    public boolean glIsTransformFeedback(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsTransformFeedback];
        return gl.glIsTransformFeedback(i);
    }

    @Override
    public void glPauseTransformFeedback() {
        verifyAccess();
        ++methodCalls[GLFunc.glPauseTransformFeedback];
        gl.glPauseTransformFeedback();
    }

    @Override
    public void glResumeTransformFeedback() {
        verifyAccess();
        ++methodCalls[GLFunc.glResumeTransformFeedback];
        gl.glResumeTransformFeedback();
    }

    @Override
    public void glGetInternalformativ(int i, int i1, int i2, int i3, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetInternalformativ];
        gl.glGetInternalformativ(i, i1, i2, i3, intBuffer);
    }

    @Override
    public void glGetInternalformativ(int i, int i1, int i2, int i3, int[] ints, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetInternalformativ];
        gl.glGetInternalformativ(i, i1, i2, i3, ints, i4);
    }

    @Override
    public void glBindImageTexture(int i, int i1, int i2, boolean b, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindImageTexture];
        gl.glBindImageTexture(i, i1, i2, b, i3, i4, i5);
    }

    @Override
    public void glMemoryBarrier(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glMemoryBarrier];
        gl.glMemoryBarrier(i);
    }

    @Override
    public void glFramebufferParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferParameteri];
        gl.glFramebufferParameteri(i, i1, i2);
    }

    @Override
    public void glGetFramebufferParameteriv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFramebufferParameteriv];
        gl.glGetFramebufferParameteriv(i, i1, intBuffer);
    }

    @Override
    public void glGetFramebufferParameteriv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetFramebufferParameteriv];
        gl.glGetFramebufferParameteriv(i, i1, ints, i2);
    }

    @Override
    public void glInvalidateFramebuffer(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateFramebuffer];
        gl.glInvalidateFramebuffer(i, i1, intBuffer);
    }

    @Override
    public void glInvalidateFramebuffer(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateFramebuffer];
        gl.glInvalidateFramebuffer(i, i1, ints, i2);
    }

    @Override
    public void glInvalidateSubFramebuffer(int i, int i1, IntBuffer intBuffer, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateSubFramebuffer];
        gl.glInvalidateSubFramebuffer(i, i1, intBuffer, i2, i3, i4, i5);
    }

    @Override
    public void glInvalidateSubFramebuffer(int i, int i1, int[] ints, int i2, int i3, int i4, int i5, int i6) {
        verifyAccess();
        ++methodCalls[GLFunc.glInvalidateSubFramebuffer];
        gl.glInvalidateSubFramebuffer(i, i1, ints, i2, i3, i4, i5, i6);
    }

    @Override
    public void glTexStorage2DMultisample(int i, int i1, int i2, int i3, int i4, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexStorage2DMultisample];
        gl.glTexStorage2DMultisample(i, i1, i2, i3, i4, b);
    }

    @Override
    public void glTexStorage3DMultisample(int i, int i1, int i2, int i3, int i4, int i5, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexStorage3DMultisample];
        gl.glTexStorage3DMultisample(i, i1, i2, i3, i4, i5, b);
    }

    @Override
    public void glGetnUniformuiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformuiv];
        gl.glGetnUniformuiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glGetnUniformuiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetnUniformuiv];
        gl.glGetnUniformuiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glPrimitiveBoundingBox(float v, float v1, float v2, float v3, float v4, float v5, float v6, float v7) {
        verifyAccess();
        ++methodCalls[GLFunc.glPrimitiveBoundingBox];
        gl.glPrimitiveBoundingBox(v, v1, v2, v3, v4, v5, v6, v7);
    }

    @Override
    public void glFramebufferTextureEXT(int i, int i1, int i2, int i3) {
        verifyAccess();
        gl.glFramebufferTextureEXT(i, i1, i2, i3);
    }

    @Override
    public boolean isPBOPackBound() {
        return gl.isPBOPackBound();
    }

    @Override
    public boolean isPBOUnpackBound() {
        return gl.isPBOUnpackBound();
    }

    @Override
    public void glAttachShader(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glAttachShader];
        gl.glAttachShader(i, i1);
    }

    @Override
    public void glBindAttribLocation(int i, int i1, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindAttribLocation];
        gl.glBindAttribLocation(i, i1, s);
    }

    @Override
    public void glBlendColor(float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glBlendColor];
        gl.glBlendColor(v, v1, v2, v3);
    }

    @Override
    public void glCompileShader(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompileShader];
        gl.glCompileShader(i);
    }

    @Override
    public int glCreateProgram() {
        verifyAccess();
        ++methodCalls[GLFunc.glCreateProgram];
        return gl.glCreateProgram();
    }

    @Override
    public int glCreateShader(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glCreateShader];
        return gl.glCreateShader(i);
    }

    @Override
    public void glDeleteProgram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteProgram];
        gl.glDeleteProgram(i);
    }

    @Override
    public void glDeleteShader(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteShader];
        gl.glDeleteShader(i);
    }

    @Override
    public void glDetachShader(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDetachShader];
        gl.glDetachShader(i, i1);
    }

    @Override
    public void glDisableVertexAttribArray(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glDisableVertexAttribArray];
        gl.glDisableVertexAttribArray(i);
    }

    @Override
    public void glEnableVertexAttribArray(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEnableVertexAttribArray];
        gl.glEnableVertexAttribArray(i);
    }

    @Override
    public void glGetActiveAttrib(int i, int i1, int i2, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveAttrib];
        gl.glGetActiveAttrib(i, i1, i2, intBuffer, intBuffer1, intBuffer2, byteBuffer);
    }

    @Override
    public void glGetActiveAttrib(int i, int i1, int i2, int[] ints, int i3, int[] ints1, int i4, int[] ints2, int i5, byte[] bytes, int i6) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveAttrib];
        gl.glGetActiveAttrib(i, i1, i2, ints, i3, ints1, i4, ints2, i5, bytes, i6);
    }

    @Override
    public void glGetActiveUniform(int i, int i1, int i2, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniform];
        gl.glGetActiveUniform(i, i1, i2, intBuffer, intBuffer1, intBuffer2, byteBuffer);
    }

    @Override
    public void glGetActiveUniform(int i, int i1, int i2, int[] ints, int i3, int[] ints1, int i4, int[] ints2, int i5, byte[] bytes, int i6) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetActiveUniform];
        gl.glGetActiveUniform(i, i1, i2, ints, i3, ints1, i4, ints2, i5, bytes, i6);
    }

    @Override
    public void glGetAttachedShaders(int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetAttachedShaders];
        gl.glGetAttachedShaders(i, i1, intBuffer, intBuffer1);
    }

    @Override
    public void glGetAttachedShaders(int i, int i1, int[] ints, int i2, int[] ints1, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetAttachedShaders];
        gl.glGetAttachedShaders(i, i1, ints, i2, ints1, i3);
    }

    @Override
    public int glGetAttribLocation(int i, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetAttribLocation];
        return gl.glGetAttribLocation(i, s);
    }

    @Override
    public void glGetProgramiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramiv];
        gl.glGetProgramiv(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramiv];
        gl.glGetProgramiv(i, i1, ints, i2);
    }

    @Override
    public void glGetProgramInfoLog(int i, int i1, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramInfoLog];
        gl.glGetProgramInfoLog(i, i1, intBuffer, byteBuffer);
    }

    @Override
    public void glGetProgramInfoLog(int i, int i1, int[] ints, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramInfoLog];
        gl.glGetProgramInfoLog(i, i1, ints, i2, bytes, i3);
    }

    @Override
    public void glGetShaderiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderiv];
        gl.glGetShaderiv(i, i1, intBuffer);
    }

    @Override
    public void glGetShaderiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderiv];
        gl.glGetShaderiv(i, i1, ints, i2);
    }

    @Override
    public void glGetShaderInfoLog(int i, int i1, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderInfoLog];
        gl.glGetShaderInfoLog(i, i1, intBuffer, byteBuffer);
    }

    @Override
    public void glGetShaderInfoLog(int i, int i1, int[] ints, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderInfoLog];
        gl.glGetShaderInfoLog(i, i1, ints, i2, bytes, i3);
    }

    @Override
    public void glGetShaderSource(int i, int i1, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderSource];
        gl.glGetShaderSource(i, i1, intBuffer, byteBuffer);
    }

    @Override
    public void glGetShaderSource(int i, int i1, int[] ints, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderSource];
        gl.glGetShaderSource(i, i1, ints, i2, bytes, i3);
    }

    @Override
    public void glGetUniformfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformfv];
        gl.glGetUniformfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetUniformfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformfv];
        gl.glGetUniformfv(i, i1, floats, i2);
    }

    @Override
    public void glGetUniformiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformiv];
        gl.glGetUniformiv(i, i1, intBuffer);
    }

    @Override
    public void glGetUniformiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformiv];
        gl.glGetUniformiv(i, i1, ints, i2);
    }

    @Override
    public int glGetUniformLocation(int i, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetUniformLocation];
        return gl.glGetUniformLocation(i, s);
    }

    @Override
    public void glGetVertexAttribfv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribfv];
        gl.glGetVertexAttribfv(i, i1, floatBuffer);
    }

    @Override
    public void glGetVertexAttribfv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribfv];
        gl.glGetVertexAttribfv(i, i1, floats, i2);
    }

    @Override
    public void glGetVertexAttribiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribiv];
        gl.glGetVertexAttribiv(i, i1, intBuffer);
    }

    @Override
    public void glGetVertexAttribiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetVertexAttribiv];
        gl.glGetVertexAttribiv(i, i1, ints, i2);
    }

    @Override
    public boolean glIsProgram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsProgram];
        return gl.glIsProgram(i);
    }

    @Override
    public boolean glIsShader(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsShader];
        return gl.glIsShader(i);
    }

    @Override
    public void glLinkProgram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glLinkProgram];
        gl.glLinkProgram(i);
    }

    @Override
    public void glShaderSource(int i, int i1, String[] strings, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glShaderSource];
        gl.glShaderSource(i, i1, strings, intBuffer);
    }

    @Override
    public void glShaderSource(int i, int i1, String[] strings, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glShaderSource];
        gl.glShaderSource(i, i1, strings, ints, i2);
    }

    @Override
    public void glStencilFuncSeparate(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilFuncSeparate];
        gl.glStencilFuncSeparate(i, i1, i2, i3);
    }

    @Override
    public void glStencilMaskSeparate(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilMaskSeparate];
        gl.glStencilMaskSeparate(i, i1);
    }

    @Override
    public void glStencilOpSeparate(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glStencilOpSeparate];
        gl.glStencilOpSeparate(i, i1, i2, i3);
    }

    @Override
    public void glUniform1f(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1f];
        gl.glUniform1f(i, v);
    }

    @Override
    public void glUniform1fv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1fv];
        gl.glUniform1fv(i, i1, floatBuffer);
    }

    @Override
    public void glUniform1fv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1fv];
        gl.glUniform1fv(i, i1, floats, i2);
    }

    @Override
    public void glUniform1i(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1i];
        gl.glUniform1i(i, i1);
    }

    @Override
    public void glUniform1iv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1iv];
        gl.glUniform1iv(i, i1, intBuffer);
    }

    @Override
    public void glUniform1iv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform1iv];
        gl.glUniform1iv(i, i1, ints, i2);
    }

    @Override
    public void glUniform2f(int i, float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2f];
        gl.glUniform2f(i, v, v1);
    }

    @Override
    public void glUniform2fv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2fv];
        gl.glUniform2fv(i, i1, floatBuffer);
    }

    @Override
    public void glUniform2fv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2fv];
        gl.glUniform2fv(i, i1, floats, i2);
    }

    @Override
    public void glUniform2i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2i];
        gl.glUniform2i(i, i1, i2);
    }

    @Override
    public void glUniform2iv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2iv];
        gl.glUniform2iv(i, i1, intBuffer);
    }

    @Override
    public void glUniform2iv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform2iv];
        gl.glUniform2iv(i, i1, ints, i2);
    }

    @Override
    public void glUniform3f(int i, float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3f];
        gl.glUniform3f(i, v, v1, v2);
    }

    @Override
    public void glUniform3fv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3fv];
        gl.glUniform3fv(i, i1, floatBuffer);
    }

    @Override
    public void glUniform3fv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3fv];
        gl.glUniform3fv(i, i1, floats, i2);
    }

    @Override
    public void glUniform3i(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3i];
        gl.glUniform3i(i, i1, i2, i3);
    }

    @Override
    public void glUniform3iv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3iv];
        gl.glUniform3iv(i, i1, intBuffer);
    }

    @Override
    public void glUniform3iv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform3iv];
        gl.glUniform3iv(i, i1, ints, i2);
    }

    @Override
    public void glUniform4f(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4f];
        gl.glUniform4f(i, v, v1, v2, v3);
    }

    @Override
    public void glUniform4fv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4fv];
        gl.glUniform4fv(i, i1, floatBuffer);
    }

    @Override
    public void glUniform4fv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4fv];
        gl.glUniform4fv(i, i1, floats, i2);
    }

    @Override
    public void glUniform4i(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4i];
        gl.glUniform4i(i, i1, i2, i3, i4);
    }

    @Override
    public void glUniform4iv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4iv];
        gl.glUniform4iv(i, i1, intBuffer);
    }

    @Override
    public void glUniform4iv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform4iv];
        gl.glUniform4iv(i, i1, ints, i2);
    }

    @Override
    public void glUniformMatrix2fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2fv];
        gl.glUniformMatrix2fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix2fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix2fv];
        gl.glUniformMatrix2fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix3fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3fv];
        gl.glUniformMatrix3fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix3fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix3fv];
        gl.glUniformMatrix3fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUniformMatrix4fv(int i, int i1, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4fv];
        gl.glUniformMatrix4fv(i, i1, b, floatBuffer);
    }

    @Override
    public void glUniformMatrix4fv(int i, int i1, boolean b, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniformMatrix4fv];
        gl.glUniformMatrix4fv(i, i1, b, floats, i2);
    }

    @Override
    public void glUseProgram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glUseProgram];
        gl.glUseProgram(i);
    }

    @Override
    public void glValidateProgram(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glValidateProgram];
        gl.glValidateProgram(i);
    }

    @Override
    public void glVertexAttrib1f(int i, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1f];
        gl.glVertexAttrib1f(i, v);
    }

    @Override
    public void glVertexAttrib1fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1fv];
        gl.glVertexAttrib1fv(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib1fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib1fv];
        gl.glVertexAttrib1fv(i, floats, i1);
    }

    @Override
    public void glVertexAttrib2f(int i, float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2f];
        gl.glVertexAttrib2f(i, v, v1);
    }

    @Override
    public void glVertexAttrib2fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2fv];
        gl.glVertexAttrib2fv(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib2fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib2fv];
        gl.glVertexAttrib2fv(i, floats, i1);
    }

    @Override
    public void glVertexAttrib3f(int i, float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3f];
        gl.glVertexAttrib3f(i, v, v1, v2);
    }

    @Override
    public void glVertexAttrib3fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3fv];
        gl.glVertexAttrib3fv(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib3fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib3fv];
        gl.glVertexAttrib3fv(i, floats, i1);
    }

    @Override
    public void glVertexAttrib4f(int i, float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4f];
        gl.glVertexAttrib4f(i, v, v1, v2, v3);
    }

    @Override
    public void glVertexAttrib4fv(int i, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4fv];
        gl.glVertexAttrib4fv(i, floatBuffer);
    }

    @Override
    public void glVertexAttrib4fv(int i, float[] floats, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttrib4fv];
        gl.glVertexAttrib4fv(i, floats, i1);
    }

    @Override
    public void glVertexAttribPointer(int i, int i1, int i2, boolean b, int i3, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribPointer];
        gl.glVertexAttribPointer(i, i1, i2, b, i3, l);
    }

    @Override
    public void glTexImage2DMultisample(int i, int i1, int i2, int i3, int i4, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage2DMultisample];
        gl.glTexImage2DMultisample(i, i1, i2, i3, i4, b);
    }

    @Override
    public void glTexImage3DMultisample(int i, int i1, int i2, int i3, int i4, int i5, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage3DMultisample];
        gl.glTexImage3DMultisample(i, i1, i2, i3, i4, i5, b);
    }

    @Override
    public void glGetMultisamplefv(int i, int i1, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMultisamplefv];
        gl.glGetMultisamplefv(i, i1, floatBuffer);
    }

    @Override
    public void glGetMultisamplefv(int i, int i1, float[] floats, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetMultisamplefv];
        gl.glGetMultisamplefv(i, i1, floats, i2);
    }

    @Override
    public void glSampleMaski(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glSampleMaski];
        gl.glSampleMaski(i, i1);
    }

    @Override
    public void glDebugMessageControl(int i, int i1, int i2, int i3, IntBuffer intBuffer, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glDebugMessageControl];
        gl.glDebugMessageControl(i, i1, i2, i3, intBuffer, b);
    }

    @Override
    public void glDebugMessageControl(int i, int i1, int i2, int i3, int[] ints, int i4, boolean b) {
        verifyAccess();
        ++methodCalls[GLFunc.glDebugMessageControl];
        gl.glDebugMessageControl(i, i1, i2, i3, ints, i4, b);
    }

    @Override
    public void glDebugMessageInsert(int i, int i1, int i2, int i3, int i4, String s) {
        verifyAccess();
        ++methodCalls[GLFunc.glDebugMessageInsert];
        gl.glDebugMessageInsert(i, i1, i2, i3, i4, s);
    }

    @Override
    public int glGetDebugMessageLog(int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1, IntBuffer intBuffer2, IntBuffer intBuffer3, IntBuffer intBuffer4, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetDebugMessageLog];
        return gl.glGetDebugMessageLog(i, i1, intBuffer, intBuffer1, intBuffer2, intBuffer3, intBuffer4, byteBuffer);
    }

    @Override
    public int glGetDebugMessageLog(int i, int i1, int[] ints, int i2, int[] ints1, int i3, int[] ints2, int i4, int[] ints3, int i5, int[] ints4, int i6, byte[] bytes, int i7) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetDebugMessageLog];
        return gl.glGetDebugMessageLog(i, i1, ints, i2, ints1, i3, ints2, i4, ints3, i5, ints4, i6, bytes, i7);
    }

    @Override
    public void glPushDebugGroup(int i, int i1, int i2, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glPushDebugGroup];
        gl.glPushDebugGroup(i, i1, i2, byteBuffer);
    }

    @Override
    public void glPushDebugGroup(int i, int i1, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glPushDebugGroup];
        gl.glPushDebugGroup(i, i1, i2, bytes, i3);
    }

    @Override
    public void glPopDebugGroup() {
        verifyAccess();
        ++methodCalls[GLFunc.glPopDebugGroup];
        gl.glPopDebugGroup();
    }

    @Override
    public void glObjectLabel(int i, int i1, int i2, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glObjectLabel];
        gl.glObjectLabel(i, i1, i2, byteBuffer);
    }

    @Override
    public void glObjectLabel(int i, int i1, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glObjectLabel];
        gl.glObjectLabel(i, i1, i2, bytes, i3);
    }

    @Override
    public void glGetObjectLabel(int i, int i1, int i2, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetObjectLabel];
        gl.glGetObjectLabel(i, i1, i2, intBuffer, byteBuffer);
    }

    @Override
    public void glGetObjectLabel(int i, int i1, int i2, int[] ints, int i3, byte[] bytes, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetObjectLabel];
        gl.glGetObjectLabel(i, i1, i2, ints, i3, bytes, i4);
    }

    @Override
    public void glObjectPtrLabel(Buffer buffer, int i, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glObjectPtrLabel];
        gl.glObjectPtrLabel(buffer, i, byteBuffer);
    }

    @Override
    public void glObjectPtrLabel(Buffer buffer, int i, byte[] bytes, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glObjectPtrLabel];
        gl.glObjectPtrLabel(buffer, i, bytes, i1);
    }

    @Override
    public void glGetObjectPtrLabel(Buffer buffer, int i, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetObjectPtrLabel];
        gl.glGetObjectPtrLabel(buffer, i, intBuffer, byteBuffer);
    }

    @Override
    public void glGetObjectPtrLabel(Buffer buffer, int i, int[] ints, int i1, byte[] bytes, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetObjectPtrLabel];
        gl.glGetObjectPtrLabel(buffer, i, ints, i1, bytes, i2);
    }

    @Override
    public void glCopyImageSubData(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10, int i11, int i12, int i13, int i14) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyImageSubData];
        gl.glCopyImageSubData(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, i10, i11, i12, i13, i14);
    }

    @Override
    public void glGetProgramBinary(int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramBinary];
        gl.glGetProgramBinary(i, i1, intBuffer, intBuffer1, buffer);
    }

    @Override
    public void glGetProgramBinary(int i, int i1, int[] ints, int i2, int[] ints1, int i3, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramBinary];
        gl.glGetProgramBinary(i, i1, ints, i2, ints1, i3, buffer);
    }

    @Override
    public void glProgramBinary(int i, int i1, Buffer buffer, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramBinary];
        gl.glProgramBinary(i, i1, buffer, i2);
    }

    @Override
    public void glTexImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage3D];
        gl.glTexImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, buffer);
    }

    @Override
    public void glTexImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexImage3D];
        gl.glTexImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, l);
    }

    @Override
    public void glTexSubImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage3D];
        gl.glTexSubImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    @Override
    public void glTexSubImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexSubImage3D];
        gl.glTexSubImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, l);
    }

    @Override
    public void glCopyTexSubImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        verifyAccess();
        ++methodCalls[GLFunc.glCopyTexSubImage3D];
        gl.glCopyTexSubImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8);
    }

    @Override
    public void glCompressedTexImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage3D];
        gl.glCompressedTexImage3D(i, i1, i2, i3, i4, i5, i6, i7, buffer);
    }

    @Override
    public void glCompressedTexImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexImage3D];
        gl.glCompressedTexImage3D(i, i1, i2, i3, i4, i5, i6, i7, l);
    }

    @Override
    public void glCompressedTexSubImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Buffer buffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage3D];
        gl.glCompressedTexSubImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, buffer);
    }

    @Override
    public void glCompressedTexSubImage3D(int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, long l) {
        verifyAccess();
        ++methodCalls[GLFunc.glCompressedTexSubImage3D];
        gl.glCompressedTexSubImage3D(i, i1, i2, i3, i4, i5, i6, i7, i8, i9, l);
    }

    @Override
    public void glFramebufferTexture3D(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glFramebufferTexture3D];
        gl.glFramebufferTexture3D(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glTexParameterIiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterIiv];
        gl.glTexParameterIiv(i, i1, intBuffer);
    }

    @Override
    public void glTexParameterIiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterIiv];
        gl.glTexParameterIiv(i, i1, ints, i2);
    }

    @Override
    public void glTexParameterIuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterIuiv];
        gl.glTexParameterIuiv(i, i1, intBuffer);
    }

    @Override
    public void glTexParameterIuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glTexParameterIuiv];
        gl.glTexParameterIuiv(i, i1, ints, i2);
    }

    @Override
    public void glGetTexParameterIiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterIiv];
        gl.glGetTexParameterIiv(i, i1, intBuffer);
    }

    @Override
    public void glGetTexParameterIiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterIiv];
        gl.glGetTexParameterIiv(i, i1, ints, i2);
    }

    @Override
    public void glGetTexParameterIuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterIuiv];
        gl.glGetTexParameterIuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetTexParameterIuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetTexParameterIuiv];
        gl.glGetTexParameterIuiv(i, i1, ints, i2);
    }

    @Override
    public void glSamplerParameterIiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glSamplerParameterIiv];
        gl.glSamplerParameterIiv(i, i1, intBuffer);
    }

    @Override
    public void glSamplerParameterIiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glSamplerParameterIiv];
        gl.glSamplerParameterIiv(i, i1, ints, i2);
    }

    @Override
    public void glSamplerParameterIuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glSamplerParameterIuiv];
        gl.glSamplerParameterIuiv(i, i1, intBuffer);
    }

    @Override
    public void glSamplerParameterIuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glSamplerParameterIuiv];
        gl.glSamplerParameterIuiv(i, i1, ints, i2);
    }

    @Override
    public void glGetSamplerParameterIiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSamplerParameterIiv];
        gl.glGetSamplerParameterIiv(i, i1, intBuffer);
    }

    @Override
    public void glGetSamplerParameterIiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSamplerParameterIiv];
        gl.glGetSamplerParameterIiv(i, i1, ints, i2);
    }

    @Override
    public void glGetSamplerParameterIuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSamplerParameterIuiv];
        gl.glGetSamplerParameterIuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetSamplerParameterIuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetSamplerParameterIuiv];
        gl.glGetSamplerParameterIuiv(i, i1, ints, i2);
    }

    @Override
    public void glDrawArraysInstancedBaseInstance(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawArraysInstancedBaseInstance];
        gl.glDrawArraysInstancedBaseInstance(i, i1, i2, i3, i4);
    }

    @Override
    public void glDrawElementsInstancedBaseInstance(int i, int i1, int i2, long l, int i3, int i4) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElementsInstancedBaseInstance];
        gl.glDrawElementsInstancedBaseInstance(i, i1, i2, l, i3, i4);
    }

    @Override
    public void glDrawElementsInstancedBaseVertexBaseInstance(int i, int i1, int i2, long l, int i3, int i4, int i5) {
        verifyAccess();
        if (drawingDisabled) return;
        ++methodCalls[GLFunc.glDrawElementsInstancedBaseVertexBaseInstance];
        gl.glDrawElementsInstancedBaseVertexBaseInstance(i, i1, i2, l, i3, i4, i5);
    }

    @Override
    public void glGenQueries(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenQueries];
        gl.glGenQueries(i, intBuffer);
    }

    @Override
    public void glGenQueries(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenQueries];
        gl.glGenQueries(i, ints, i1);
    }

    @Override
    public void glDeleteQueries(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteQueries];
        gl.glDeleteQueries(i, intBuffer);
    }

    @Override
    public void glDeleteQueries(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteQueries];
        gl.glDeleteQueries(i, ints, i1);
    }

    @Override
    public boolean glIsQuery(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsQuery];
        return gl.glIsQuery(i);
    }

    @Override
    public void glBeginQuery(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glBeginQuery];
        gl.glBeginQuery(i, i1);
    }

    @Override
    public void glEndQuery(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glEndQuery];
        gl.glEndQuery(i);
    }

    @Override
    public void glQueryCounter(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glQueryCounter];
        gl.glQueryCounter(i, i1);
    }

    @Override
    public void glGetQueryiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryiv];
        gl.glGetQueryiv(i, i1, intBuffer);
    }

    @Override
    public void glGetQueryiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryiv];
        gl.glGetQueryiv(i, i1, ints, i2);
    }

    @Override
    public void glGetQueryObjectiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectiv];
        gl.glGetQueryObjectiv(i, i1, intBuffer);
    }

    @Override
    public void glGetQueryObjectiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectiv];
        gl.glGetQueryObjectiv(i, i1, ints, i2);
    }

    @Override
    public void glGetQueryObjectuiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectuiv];
        gl.glGetQueryObjectuiv(i, i1, intBuffer);
    }

    @Override
    public void glGetQueryObjectuiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectuiv];
        gl.glGetQueryObjectuiv(i, i1, ints, i2);
    }

    @Override
    public void glGetQueryObjecti64v(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjecti64v];
        gl.glGetQueryObjecti64v(i, i1, longBuffer);
    }

    @Override
    public void glGetQueryObjecti64v(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjecti64v];
        gl.glGetQueryObjecti64v(i, i1, longs, i2);
    }

    @Override
    public void glGetQueryObjectui64v(int i, int i1, LongBuffer longBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectui64v];
        gl.glGetQueryObjectui64v(i, i1, longBuffer);
    }

    @Override
    public void glGetQueryObjectui64v(int i, int i1, long[] longs, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetQueryObjectui64v];
        gl.glGetQueryObjectui64v(i, i1, longs, i2);
    }

    @Override
    public void glActiveShaderProgram(int i, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glActiveShaderProgram];
        gl.glActiveShaderProgram(i, i1);
    }

    @Override
    public void glBindProgramPipeline(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glBindProgramPipeline];
        gl.glBindProgramPipeline(i);
    }

    @Override
    public int glCreateShaderProgramv(int i, int i1, String[] strings) {
        verifyAccess();
        ++methodCalls[GLFunc.glCreateShaderProgramv];
        return gl.glCreateShaderProgramv(i, i1, strings);
    }

    @Override
    public void glDeleteProgramPipelines(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteProgramPipelines];
        gl.glDeleteProgramPipelines(i, intBuffer);
    }

    @Override
    public void glDeleteProgramPipelines(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDeleteProgramPipelines];
        gl.glDeleteProgramPipelines(i, ints, i1);
    }

    @Override
    public void glGenProgramPipelines(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenProgramPipelines];
        gl.glGenProgramPipelines(i, intBuffer);
    }

    @Override
    public void glGenProgramPipelines(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGenProgramPipelines];
        gl.glGenProgramPipelines(i, ints, i1);
    }

    @Override
    public void glGetProgramPipelineInfoLog(int i, int i1, IntBuffer intBuffer, ByteBuffer byteBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramPipelineInfoLog];
        gl.glGetProgramPipelineInfoLog(i, i1, intBuffer, byteBuffer);
    }

    @Override
    public void glGetProgramPipelineInfoLog(int i, int i1, int[] ints, int i2, byte[] bytes, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramPipelineInfoLog];
        gl.glGetProgramPipelineInfoLog(i, i1, ints, i2, bytes, i3);
    }

    @Override
    public void glGetProgramPipelineiv(int i, int i1, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramPipelineiv];
        gl.glGetProgramPipelineiv(i, i1, intBuffer);
    }

    @Override
    public void glGetProgramPipelineiv(int i, int i1, int[] ints, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetProgramPipelineiv];
        gl.glGetProgramPipelineiv(i, i1, ints, i2);
    }

    @Override
    public boolean glIsProgramPipeline(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glIsProgramPipeline];
        return gl.glIsProgramPipeline(i);
    }

    @Override
    public void glProgramParameteri(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramParameteri];
        gl.glProgramParameteri(i, i1, i2);
    }

    @Override
    public void glProgramUniform1f(int i, int i1, float v) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1f];
        gl.glProgramUniform1f(i, i1, v);
    }

    @Override
    public void glProgramUniform1fv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1fv];
        gl.glProgramUniform1fv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramUniform1fv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1fv];
        gl.glProgramUniform1fv(i, i1, i2, floats, i3);
    }

    @Override
    public void glProgramUniform1i(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1i];
        gl.glProgramUniform1i(i, i1, i2);
    }

    @Override
    public void glProgramUniform1iv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1iv];
        gl.glProgramUniform1iv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform1iv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1iv];
        gl.glProgramUniform1iv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform2f(int i, int i1, float v, float v1) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2f];
        gl.glProgramUniform2f(i, i1, v, v1);
    }

    @Override
    public void glProgramUniform2fv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2fv];
        gl.glProgramUniform2fv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramUniform2fv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2fv];
        gl.glProgramUniform2fv(i, i1, i2, floats, i3);
    }

    @Override
    public void glProgramUniform2i(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2i];
        gl.glProgramUniform2i(i, i1, i2, i3);
    }

    @Override
    public void glProgramUniform2iv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2iv];
        gl.glProgramUniform2iv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform2iv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2iv];
        gl.glProgramUniform2iv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform3f(int i, int i1, float v, float v1, float v2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3f];
        gl.glProgramUniform3f(i, i1, v, v1, v2);
    }

    @Override
    public void glProgramUniform3fv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3fv];
        gl.glProgramUniform3fv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramUniform3fv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3fv];
        gl.glProgramUniform3fv(i, i1, i2, floats, i3);
    }

    @Override
    public void glProgramUniform3i(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3i];
        gl.glProgramUniform3i(i, i1, i2, i3, i4);
    }

    @Override
    public void glProgramUniform3iv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3iv];
        gl.glProgramUniform3iv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform3iv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3iv];
        gl.glProgramUniform3iv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform4f(int i, int i1, float v, float v1, float v2, float v3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4f];
        gl.glProgramUniform4f(i, i1, v, v1, v2, v3);
    }

    @Override
    public void glProgramUniform4fv(int i, int i1, int i2, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4fv];
        gl.glProgramUniform4fv(i, i1, i2, floatBuffer);
    }

    @Override
    public void glProgramUniform4fv(int i, int i1, int i2, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4fv];
        gl.glProgramUniform4fv(i, i1, i2, floats, i3);
    }

    @Override
    public void glProgramUniform4i(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4i];
        gl.glProgramUniform4i(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramUniform4iv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4iv];
        gl.glProgramUniform4iv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform4iv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4iv];
        gl.glProgramUniform4iv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniformMatrix2fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2fv];
        gl.glProgramUniformMatrix2fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix2fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2fv];
        gl.glProgramUniformMatrix2fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix3fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3fv];
        gl.glProgramUniformMatrix3fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix3fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3fv];
        gl.glProgramUniformMatrix3fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix4fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4fv];
        gl.glProgramUniformMatrix4fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix4fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4fv];
        gl.glProgramUniformMatrix4fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glUseProgramStages(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glUseProgramStages];
        gl.glUseProgramStages(i, i1, i2);
    }

    @Override
    public void glValidateProgramPipeline(int i) {
        verifyAccess();
        ++methodCalls[GLFunc.glValidateProgramPipeline];
        gl.glValidateProgramPipeline(i);
    }

    @Override
    public void glProgramUniform1ui(int i, int i1, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1ui];
        gl.glProgramUniform1ui(i, i1, i2);
    }

    @Override
    public void glProgramUniform2ui(int i, int i1, int i2, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2ui];
        gl.glProgramUniform2ui(i, i1, i2, i3);
    }

    @Override
    public void glProgramUniform3ui(int i, int i1, int i2, int i3, int i4) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3ui];
        gl.glProgramUniform3ui(i, i1, i2, i3, i4);
    }

    @Override
    public void glProgramUniform4ui(int i, int i1, int i2, int i3, int i4, int i5) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4ui];
        gl.glProgramUniform4ui(i, i1, i2, i3, i4, i5);
    }

    @Override
    public void glProgramUniform1uiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1uiv];
        gl.glProgramUniform1uiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform1uiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform1uiv];
        gl.glProgramUniform1uiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform2uiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2uiv];
        gl.glProgramUniform2uiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform2uiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform2uiv];
        gl.glProgramUniform2uiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform3uiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3uiv];
        gl.glProgramUniform3uiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform3uiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform3uiv];
        gl.glProgramUniform3uiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniform4uiv(int i, int i1, int i2, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4uiv];
        gl.glProgramUniform4uiv(i, i1, i2, intBuffer);
    }

    @Override
    public void glProgramUniform4uiv(int i, int i1, int i2, int[] ints, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniform4uiv];
        gl.glProgramUniform4uiv(i, i1, i2, ints, i3);
    }

    @Override
    public void glProgramUniformMatrix2x3fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x3fv];
        gl.glProgramUniformMatrix2x3fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x3fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x3fv];
        gl.glProgramUniformMatrix2x3fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix3x2fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x2fv];
        gl.glProgramUniformMatrix3x2fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x2fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x2fv];
        gl.glProgramUniformMatrix3x2fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix2x4fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x4fv];
        gl.glProgramUniformMatrix2x4fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix2x4fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix2x4fv];
        gl.glProgramUniformMatrix2x4fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix4x2fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x2fv];
        gl.glProgramUniformMatrix4x2fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x2fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x2fv];
        gl.glProgramUniformMatrix4x2fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix3x4fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x4fv];
        gl.glProgramUniformMatrix3x4fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix3x4fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix3x4fv];
        gl.glProgramUniformMatrix3x4fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glProgramUniformMatrix4x3fv(int i, int i1, int i2, boolean b, FloatBuffer floatBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x3fv];
        gl.glProgramUniformMatrix4x3fv(i, i1, i2, b, floatBuffer);
    }

    @Override
    public void glProgramUniformMatrix4x3fv(int i, int i1, int i2, boolean b, float[] floats, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glProgramUniformMatrix4x3fv];
        gl.glProgramUniformMatrix4x3fv(i, i1, i2, b, floats, i3);
    }

    @Override
    public void glApplyFramebufferAttachmentCMAAINTEL() {
        verifyAccess();
        gl.glApplyFramebufferAttachmentCMAAINTEL();
    }

    @Override
    public void glDrawBuffers(int i, IntBuffer intBuffer) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawBuffers];
        gl.glDrawBuffers(i, intBuffer);
    }

    @Override
    public void glDrawBuffers(int i, int[] ints, int i1) {
        verifyAccess();
        ++methodCalls[GLFunc.glDrawBuffers];
        gl.glDrawBuffers(i, ints, i1);
    }

    @Override
    public void glReleaseShaderCompiler() {
        verifyAccess();
        ++methodCalls[GLFunc.glReleaseShaderCompiler];
        gl.glReleaseShaderCompiler();
    }

    @Override
    public void glShaderBinary(int i, IntBuffer intBuffer, int i1, Buffer buffer, int i2) {
        verifyAccess();
        ++methodCalls[GLFunc.glShaderBinary];
        gl.glShaderBinary(i, intBuffer, i1, buffer, i2);
    }

    @Override
    public void glShaderBinary(int i, int[] ints, int i1, int i2, Buffer buffer, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glShaderBinary];
        gl.glShaderBinary(i, ints, i1, i2, buffer, i3);
    }

    @Override
    public void glGetShaderPrecisionFormat(int i, int i1, IntBuffer intBuffer, IntBuffer intBuffer1) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderPrecisionFormat];
        gl.glGetShaderPrecisionFormat(i, i1, intBuffer, intBuffer1);
    }

    @Override
    public void glGetShaderPrecisionFormat(int i, int i1, int[] ints, int i2, int[] ints1, int i3) {
        verifyAccess();
        ++methodCalls[GLFunc.glGetShaderPrecisionFormat];
        gl.glGetShaderPrecisionFormat(i, i1, ints, i2, ints1, i3);
    }

    @Override
    public void glVertexAttribPointer(GLArrayData glArrayData) {
        verifyAccess();
        ++methodCalls[GLFunc.glVertexAttribPointer];
        gl.glVertexAttribPointer(glArrayData);
    }

    @Override
    public void glUniform(GLUniformData glUniformData) {
        verifyAccess();
        ++methodCalls[GLFunc.glUniform];
        gl.glUniform(glUniformData);
    }

}
