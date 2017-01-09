**Work in Progress, APIs and extension points are unstable**

# tlvVisualizer

[![Build Status](https://travis-ci.org/andreasWallner/tlvVisualizer.svg)](https://travis-ci.org/andreasWallner/tlvVisualizer)

A simple simple Eclipse plugin developed more to get into Eclipse stuff than anything
else.

Currently allows one to parse TLV (tag-length-value) data, and displays some additional
information based on the tag types defined by EMVco.

# Binary versions
Binary versions of TLVVisualizer can be found at https://bintray.com/andreaswallner/TLVVisualizer/releases
as P2 update sites. Use https://dl.bintray.com/andreaswallner/TLVVisualizer in Eclipse to install.
The binary version is currently most likely outdated because of ongoing development.

# Features
* Parse arbitrary BER-TLVs
* Usable as library
* Decodes TLVs used in EMVco systems
** Decodes down to bitlevel for tags with bitfields
* Decodes S(Parameter) TLVs as specified in ISO 14443-4:2016
** Decodes down to bitlevel using tooltips

# Example TLVs to try
## EMV
    6F1A840E315041592E5359532E4444463031A5088801025F2D02656E
    8407A0000000041010A50F500A4D617374657243617264870101
## S(PARAMETER)
(Try hovering over bitfields)

    A002 A100
    A00A A208 80021900 80024900
    A00A A308 83021000 84024000
    A00E A60C 800103 810103 820107 830107    

# Todo (...Ideas)
 - fix TODOs in code...
 - TLVViewer: combobox to select ID by name
 - TLVViewer: verify and show incorrect tree (subelements not allowed in hierarchy)
 - TLVViewer: don't change tree visibility on e.g. setTlvString if not necessary
 - is the tlvvisualizer.views sub-package really needed?
 - cleanup what is in which package (emv decoder to subnamespace, valuedecoder to tlv, ...) 
 - context specific add menu (show tags allowed under current tag, depending on decoder)
 - colorize fields on invalid length, etc.
 - bring back copy of formatted text back

# Eclipse setup
- make sure you have `m2e - Maven Integration for Eclipse` installed
- import all projects into eclipse
- use `Window`/`Preferences`/`Plug-in Development`/`Target Platform` to set
  target platform in `at.innovative-solutions.tlvVisualizer.releng.target`
- build/run/debug  

# Running build/tests
To run the build, either use the command line to run the master POM `./pom.xml` file, or when using
Eclipse do a maven build of the `at.innovative-solutions.tlvVisualizer.releng` project.
Common options are `clean verify`. After the first build, the `offline` option can be
set to reduce download volume.

When trying to run unit tests from Eclipse, run the test projects as JUnit Plugin tests.

# Credits
Icon made by [Freepik](http://www.freepik.com) from [www.flaticon.com](http://www.flaticon.com) is licensed by [CC 3.0 BY](http://creativecommons.org/licenses/by/3.0/)
