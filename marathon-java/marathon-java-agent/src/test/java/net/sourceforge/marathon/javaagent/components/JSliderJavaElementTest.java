/*******************************************************************************
 * Copyright 2016 Jalian Systems Pvt. Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.sourceforge.marathon.javaagent.components;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import components.SliderDemo;
import net.sourceforge.marathon.javaagent.IJavaAgent;
import net.sourceforge.marathon.javaagent.IJavaElement;
import net.sourceforge.marathon.javaagent.JavaAgent;
import net.sourceforge.marathon.javaagent.JavaElementFactory;

@Test
public class JSliderJavaElementTest {
    private IJavaAgent driver;
    protected JFrame frame;

    @BeforeMethod
    public void showDialog() throws Throwable {
        JavaElementFactory.add(JSlider.class, JSliderJavaElement.class);
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame = new JFrame(JSliderJavaElementTest.class.getSimpleName());
                frame.setName("frame-" + JSliderJavaElementTest.class.getSimpleName());
                frame.getContentPane().add(new SliderDemo(), BorderLayout.CENTER);
                frame.pack();
                frame.setAlwaysOnTop(true);
                frame.setVisible(true);
            }
        });
        driver = new JavaAgent();
    }

    @AfterMethod
    public void disposeDriver() throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(false);
                frame.dispose();
            }
        });
    }

    public void setSliderValue() {
        IJavaElement slider = driver.findElementByTagName("slider");
        marathon_select(slider, "10");
        AssertJUnit.assertEquals("10", slider.getAttribute("value"));
    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void illegalArgumentException() {
        IJavaElement slider = driver.findElementByTagName("slider");
        marathon_select(slider, "ten");
        AssertJUnit.assertEquals("", slider.getAttribute("value"));
    }

    private void marathon_select(IJavaElement e, String state) {
        String encodedState = state.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
        e.findElementByCssSelector(".::call-select('" + encodedState + "')");
    }
}
