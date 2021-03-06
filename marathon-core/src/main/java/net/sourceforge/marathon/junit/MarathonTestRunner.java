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
package net.sourceforge.marathon.junit;

import java.util.logging.Logger;

import org.junit.runner.Computer;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import junit.runner.Version;

/**
 * <code>JUnitCore</code> is a facade for running tests. It supports running
 * JUnit 4 tests, JUnit 3.8.x tests, and mixtures. To run tests from the command
 * line, run
 * <code>java org.junit.runner.JUnitCore TestClass1 TestClass2 ...</code>. For
 * one-shot test runs, use the static method {@link #runClasses(Class[])}. If
 * you want to add special listeners, create an instance of
 * {@link org.junit.runner.JUnitCore} first and use it to run the tests.
 *
 * @see org.junit.runner.Result
 * @see org.junit.runner.notification.RunListener
 * @see org.junit.runner.Request
 * @since 4.0
 */
public class MarathonTestRunner {

    public static final Logger LOGGER = Logger.getLogger(MarathonTestRunner.class.getName());

    private final RunNotifier notifier = new RunNotifier();

    /**
     * Run the tests contained in <code>classes</code>. Write feedback while the
     * tests are running and write stack traces for all failed tests after all
     * tests complete. This is similar to {@link #main(String[])}, but intended
     * to be used programmatically.
     *
     * @param classes
     *            Classes in which to find tests
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public static Result runClasses(Class<?>... classes) {
        return runClasses(defaultComputer(), classes);
    }

    /**
     * Run the tests contained in <code>classes</code>. Write feedback while the
     * tests are running and write stack traces for all failed tests after all
     * tests complete. This is similar to {@link #main(String[])}, but intended
     * to be used programmatically.
     *
     * @param computer
     *            Helps construct Runners from classes
     * @param classes
     *            Classes in which to find tests
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public static Result runClasses(Computer computer, Class<?>... classes) {
        return new MarathonTestRunner().run(computer, classes);
    }

    /**
     * @return the version number of this release
     */
    public String getVersion() {
        return Version.id();
    }

    /**
     * Run all the tests in <code>classes</code>.
     *
     * @param classes
     *            the classes containing tests
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public Result run(Class<?>... classes) {
        return run(defaultComputer(), classes);
    }

    /**
     * Run all the tests in <code>classes</code>.
     *
     * @param computer
     *            Helps construct Runners from classes
     * @param classes
     *            the classes containing tests
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public Result run(Computer computer, Class<?>... classes) {
        return run(Request.classes(computer, classes));
    }

    /**
     * Run all the tests contained in <code>request</code>.
     *
     * @param request
     *            the request describing tests
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public Result run(Request request) {
        return run(request.getRunner());
    }

    /**
     * Run all the tests contained in JUnit 3.8.x <code>test</code>. Here for
     * backward compatibility.
     *
     * @param test
     *            the old-style test
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public Result run(junit.framework.Test test) {
        MarathonTestProvider.setSuite(test);
        return run(MarathonTestProvider.class);
    }

    /**
     * Run all the tests contained in JUnit 3.8.x <code>test</code> parallel.
     * Here for backward compatibility.
     *
     * @param test
     *            the old-style test
     * @return a {@link Result} describing the details of the test run and the
     *         failed tests.
     */
    public Result runParallel(junit.framework.Test test) {
        MarathonTestProvider.setSuite(test);
        return run(new ParallelComputer(false, true), MarathonTestProvider.class);
    }

    /**
     * Do not use. Testing purposes only.
     */
    public Result run(org.junit.runner.Runner runner) {
        Result result = new Result();
        RunListener listener = result.createListener();
        notifier.addFirstListener(listener);
        try {
            notifier.fireTestRunStarted(runner.getDescription());
            runner.run(notifier);
            notifier.fireTestRunFinished(result);
        } finally {
            removeListener(listener);
        }
        return result;
    }

    /**
     * Add a listener to be notified as the tests run.
     *
     * @param listener
     *            the listener to add
     * @see org.junit.runner.notification.RunListener
     */
    public void addListener(RunListener listener) {
        notifier.addListener(listener);
    }

    /**
     * Remove a listener.
     *
     * @param listener
     *            the listener to remove
     */
    public void removeListener(RunListener listener) {
        notifier.removeListener(listener);
    }

    static Computer defaultComputer() {
        return new Computer();
    }

    public RunNotifier getNotifier() {
        return notifier;
    }
}
