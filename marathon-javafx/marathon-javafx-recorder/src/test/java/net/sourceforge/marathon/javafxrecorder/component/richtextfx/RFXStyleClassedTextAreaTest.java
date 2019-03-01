package net.sourceforge.marathon.javafxrecorder.component.richtextfx;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import javafx.application.Platform;
import javafx.scene.layout.Pane;
import net.sourceforge.marathon.javafxagent.Wait;
import net.sourceforge.marathon.javafxagent.components.richtextfx.StyleClassedTextAreaSample;
import net.sourceforge.marathon.javafxrecorder.component.LoggingRecorder;
import net.sourceforge.marathon.javafxrecorder.component.LoggingRecorder.Recording;
import net.sourceforge.marathon.javafxrecorder.component.RFXComponent;
import net.sourceforge.marathon.javafxrecorder.component.RFXComponentTest;

public class RFXStyleClassedTextAreaTest extends RFXComponentTest {

    @Override
    protected Pane getMainPane() {
        return new StyleClassedTextAreaSample();
    }

    @Test
    public void getText() {
        final StyleClassedTextArea textArea = (StyleClassedTextArea) getPrimaryStage().getScene().getRoot()
                .lookup(".styled-text-area");
        LoggingRecorder lr = new LoggingRecorder();
        List<Object> text = new ArrayList<>();
        Platform.runLater(() -> {
            RFXComponent rca = new RFXGenericStyledArea(textArea, null, null, lr);
            textArea.appendText("Hello World");
            rca.focusLost(null);
            text.add(rca.getAttribute("text"));
        });
        new Wait("Waiting for text area text.") {
            @Override
            public boolean until() {
                return text.size() > 0;
            }
        };
        AssertJUnit.assertEquals("Hello World", text.get(0));
    }

    @Test
    public void select() {
        final StyleClassedTextArea styleClassedTextAreaNode = (StyleClassedTextArea) getPrimaryStage().getScene().getRoot()
                .lookup(".styled-text-area");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                styleClassedTextAreaNode.appendText("Hello World");
            }
        });
        LoggingRecorder lr = new LoggingRecorder();
        RFXComponent rTextField = new RFXGenericStyledArea(styleClassedTextAreaNode, null, null, lr);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                rTextField.focusLost(null);
            }
        });
        List<Recording> recordings = lr.waitAndGetRecordings(1);
        Recording select = recordings.get(0);
        AssertJUnit.assertEquals("recordSelect", select.getCall());
        AssertJUnit.assertEquals("Hello World", select.getParameters()[0]);
    }

    @Test
    public void selectWithSpecialChars() throws InterruptedException {
        final StyleClassedTextArea styleClassedTextAreaNode = (StyleClassedTextArea) getPrimaryStage().getScene().getRoot()
                .lookup(".styled-text-area");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                styleClassedTextAreaNode.appendText("Hello\n World'\"");
            }
        });
        LoggingRecorder lr = new LoggingRecorder();
        RFXComponent rTextField = new RFXGenericStyledArea(styleClassedTextAreaNode, null, null, lr);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                rTextField.focusLost(null);
            }
        });
        List<Recording> recordings = lr.waitAndGetRecordings(1);
        Recording select = recordings.get(0);
        AssertJUnit.assertEquals("recordSelect", select.getCall());
        AssertJUnit.assertEquals("Hello\n World'\"", select.getParameters()[0]);
    }

    @Test
    public void selectWithUtf8Chars() throws InterruptedException {
        final StyleClassedTextArea styleClassedTextAreaNode = (StyleClassedTextArea) getPrimaryStage().getScene().getRoot()
                .lookup(".styled-text-area");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                styleClassedTextAreaNode.appendText("å∫ç∂´ƒ©˙ˆ∆");
            }
        });
        LoggingRecorder lr = new LoggingRecorder();
        RFXComponent rTextField = new RFXGenericStyledArea(styleClassedTextAreaNode, null, null, lr);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                rTextField.focusLost(null);
            }
        });
        List<Recording> recordings = lr.waitAndGetRecordings(1);
        Recording select = recordings.get(0);
        AssertJUnit.assertEquals("recordSelect", select.getCall());
        AssertJUnit.assertEquals("å∫ç∂´ƒ©˙ˆ∆", select.getParameters()[0]);
    }
}
