#  java-playwright-browserstack-nonclosing
This repo contains samples for running junit-5 playwright tests on BrowserStack, making use of only a single session with a one-time teardown at the end (AfterAll).

## Setup
* Clone the repo `git clone https://github.com/Roger-BStack/java-playwright-browserstack-nonclosing.git`
* Update credentials in the `browserstack.yml` file with your [BrowserStack Username and Access Key](https://www.browserstack.com/accounts/settings).

## Running tests:
* To run the sample tests on BrowserStack, run `gradlew clean test -Drun-on-bstack=true`.
* To run the sample tests on your machines, run `gradlew clean test` OR `gradlew clean test -Drun-on-bstack=false`.

Understand how many parallel sessions you need by using our [Parallel Test Calculator](https://www.browserstack.com/automate/parallel-calculator?ref=github)

## Notes
* You can view your test results on the [BrowserStack Automate dashboard](https://www.browserstack.com/automate)
* You can export the environment variables for the Username and Access Key of your BrowserStack account.

    * For Unix-like or Mac machines:
  ```
  export BROWSERSTACK_USERNAME=<browserstack-username> &&
  export BROWSERSTACK_ACCESS_KEY=<browserstack-access-key>
  ```

    * For Windows:
  ```
  set BROWSERSTACK_USERNAME=<browserstack-username>
  set BROWSERSTACK_ACCESS_KEY=<browserstack-access-key>
  ```