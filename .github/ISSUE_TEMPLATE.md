<!--
WARNING: Failure to follow the issue template guidelines below will result in the issue being immediately closed.

Only bug reports/feature request/documentation issues should be opened here.

Before opening an issue, please check a similar issue is not already open (or closed). Duplicates or near-duplicates will be closed immediately.
-->

**I'm submitting a ...**  (check one with "x"):
- [ ] bug report
- [ ] feature request
- [ ] documentation issue


<!-- Fill out the relevant sections below and delete irrelevant sections. -->

# Bug report

**Current behavior:**

<!-- Describe how the bug manifests. -->

<!-- Explain how you're sure there is an issue with this plugin rather than your own code:
 - If this plugin has an example project, have you been able to reproduce the issue within it?
 - Have you created a clean test Cordova project containing only this plugin to eliminate the potential for interference with other plugins/code?
 -->

**Expected behavior:**

<!-- Describe what the behavior should be without the bug. -->

**Steps to reproduce:**

<!-- If you are able to illustrate the bug with an example, please provide steps to reproduce. -->

**Environment information**

<!-- Please supply full details of your development environment including: -->
- Cordova CLI version 
	- `cordova -v`
- Cordova platform version
	- `cordova platform ls`
- Plugins & versions installed in project (including this plugin)
    - `cordova plugin ls`
- Dev machine OS and version, e.g.
    - OSX
        - `sw_vers`
    - Windows 10
        - `winver`
        
_Runtime issue_
- Device details
    - _e.g. iPhone 7, Samsung Galaxy S8, iPhone X Simulator, Pixel XL Emulator_
- OS details
    - _e.g. iOS 11.2, Android 8.1_	
	
_Android build issue:_	
- Node JS version
    - `node -v`
- Gradle version
	- `ls platforms/android/.gradle`
- Target Android SDK version
	- `android:targetSdkVersion` in `AndroidManifest.xml`
- Android SDK details
	- `sdkmanager --list | sed -e '/Available Packages/q'`
	
_iOS build issue:_
- Node JS version
    - `node -v`
- XCode version

_If using an [Ionic Native Typescript wrapper]() for this plugin:_
- Ionic environment info
    - `ionic info`
- Installed Ionic Native modules and versions
    - `npm list | grep "@ionic-native"`

<!--
NOTE: Ionic Native Typescript wrappers are maintained by the Ionic Team:
- Any issue which is suspected of being caused by the Ionic Native wrapper should be reported against Ionic Native (https://github.com/ionic-team/ionic-native/issues)
- To verify an if an issue is caused by this plugin or its Typescript wrapper, please re-test using the vanilla Javascript plugin interface (without the Ionic Native wrapper).
- Any issue opened here which is obviously an Ionic Typescript wrapper issue will be closed immediately.
-->

**Related code:**

```
insert any relevant code here such as plugin API calls / input parameters
```

**Console output**

<details>
<summary>console output</summary>

```

// Paste any relevant JS/native console output here

```

</details><br/><br/>

**Other information:**

<!-- List any other information that is relevant to your issue. Stack traces, related issues, suggestions on how to fix, Stack Overflow links, forum links, etc. -->

# Feature request
<!--
Feature requests should include as much detail as possible:

- A descriptive title 
- A description of the problem you're trying to solve, including why you think this is a problem
- An overview of the suggested solution
- Use case: why should this be implemented?
- If the feature changes current behavior, reasons why your solution is better
- Relevant links, e.g.
    - Stack Overflow post illustrating a solution
    - Code within a Github repo that illustrates a solution
    - Native API documentation for proposed feature
-->

# Documentation issue
<!-- 
Describe the issue with the documentation or the request for documentation changes.
- Please give reasons why the change is necessary.
- If the change is trivial or you are able to make it, please consider making a Pull Request containing the necessary changes.
-->




<!--
A POLITE REMINDER

- This is free, open-source software. 
- Although the author makes every effort to maintain it, no guarantees are made as to the quality or reliability, and reported issues will be addressed if and when the author has time. 
- Help/support will not be given by the author, so forums (e.g. Ionic) or Stack Overflow should be used. Any issues requesting help/support will be closed immediately.
- If you have urgent need of a bug fix/feature, the author can be engaged for PAID contract work to do so: please contact dave@workingedge.co.uk
- Rude or abusive comments/issues will not be tolerated, nor will opening multiple issues if those previously closed are deemed unsuitable. Any of the above will result in you being BANNED from ALL of my Github repositories.
-->