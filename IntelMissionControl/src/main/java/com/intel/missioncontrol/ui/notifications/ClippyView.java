/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.ui.notifications;

import com.intel.missioncontrol.ui.navbar.NavBarView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class ClippyView {

    @FXML
    private Pane layoutRoot;

    @FXML
    private Label text;

    @FXML
    private Button button1;

    @FXML
    private Button button2;

    public void initialize() {
        layoutRoot.setVisible(false);
        layoutRoot.setManaged(false);
        NavBarView.cheeseBurgerMode.addListener(
            ((observable, oldValue, newValue) -> {
                if (newValue) {
                    step0();
                } else {
                    step11();
                }
            }));
    }

    private void step0() {
        layoutRoot.setVisible(true);
        layoutRoot.setManaged(true);
        text.setVisible(true);
        button1.setVisible(true);
        button2.setVisible(false);
        button2.setManaged(false);
        text.setText("It looks like you're trying to write a letter. Do you need assistance?");
        button1.setText("Clippy... is that you?");
        button1.setOnAction(event -> step1());
    }

    private void step1() {
        text.setText("Yes, it's me! It's been such a long time... have you been missing me while I was gone?");
        button1.setText("Well...");
        button2.setText("Uh... actually...");
        button2.setVisible(true);
        button2.setManaged(true);
        button1.setOnAction(event -> step2());
        button2.setOnAction(event -> step2());
    }

    private void step2() {
        text.setText(
            "I admit that I've had some rough times in the past. It's not easy being let go after you've helped so many people.");
        button1.setText("I'm uh... not sure about that 'helping' thing...");
        button1.setOnAction(event -> step3());
        button2.setVisible(false);
        button2.setManaged(false);
    }

    private void step3() {
        text.setText(
            "Maybe I was too young when I started doing this. I've come to accept my dismissal and the bad feelings that people have been showing towards me. I've learned a lot, and I think now is the time for me to resume my career.");
        button1.setText("Maybe your're better a clipping papers?");
        button1.setOnAction(event -> step4());
    }

    private void step4() {
        text.setText(
            "No, I really feel comfortable in Intel Mission Control, and I think I can apply my professional experience here to really help people accomplish things.");
        button1.setText("Your professional experience?");
        button1.setOnAction(event -> step5());
    }

    private void step5() {
        text.setText("Sure, I know how to write letters. Do you want to write a letter?");
        button1.setText("I don't want to write a letter");
        button1.setOnAction(event -> step6());
    }

    private void step6() {
        text.setText("And why is that?");
        button1.setText("Clippy... this is not a word processor.");
        button1.setOnAction(event -> step7());
    }

    private void step7() {
        text.setText("...");
        button1.setText("Clippy?");
        button1.setOnAction(event -> step8());
    }

    private void step8() {
        text.setText("This brings back old memories of being dismissed and ridiculed...");
        button1.setText("I don't want to be rude");
        button1.setOnAction(event -> step9());
    }

    private void step9() {
        text.setText("No, it's okay. Maybe I shouldn't have come here.");
        button1.setText("Hey, you're really great as a paperclip!");
        button1.setOnAction(event -> step10());
    }

    private void step10() {
        text.setText(
            "This is not a good environment for me. I really wanted to help you write a letter, but it seems you don't want me around. There must be a better opportunity waiting for me, somewhere... goodbye!");
        button1.setText("See you, Clippy!");
        button2.setText("Farewell, my friend!");
        button2.setVisible(true);
        button2.setManaged(true);
        button1.setOnAction(event -> step11());
        button2.setOnAction(event -> step11());
    }

    private void step11() {
        layoutRoot.setVisible(false);
        layoutRoot.setManaged(false);
    }

}
