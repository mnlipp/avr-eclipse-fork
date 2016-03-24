# avr-eclipse-fork
A fork of avr-eclipse.

This fork currently merges in its master the master from avr-eclipse as you find it on SourceForge
with branch R2.4.2 of the project on SourceForge (master hasn't been updated there). You could say 
that this is the "better" or "continued" master for the project.

The fork includes the good avrdude front-end etc. from R2.4.2 without any functional changes. I have
tested it with Luna SR2 and it works (for my use cases). You could call this part a "2.4.3"
version.

I have put the debug launch configurations from the master branch (that have been left out 
in R2.4.2) in a separate feature. So your're free to also install them of leave them out.
The launch configurations have been rebased on the now available CDT "GDB Hardware Debugging" backend. 
The benefit of having AVR specific launch configurations is that avarice is started as part of the 
launch automatically and that the dialogs focus on the options for the AVR microcontrollers. 
In addition, the launch configurations include a work around for a long standing gdb bug 
that prevents the disassemly window and breakpoints at arbitrary locations (not source code lines) 
from working properly.

The tycho/maven files have been updated with the files from R2.4.2, but they are untested. Personally, 
I think that maven is the worst thing that has ever happened to Java development environments and I don't
use it and won't use it. For building the p2 repository, simply go to the feature project and use
the export wizard (or click on "Build" in the update site project).

The "test plugins" have also not been tested. I have no idea what they actually do and whether they have
ever worked properly.

The p2 repository is kept in a branch. You can add it as update site using the [https://raw.githubusercontent.com/mnlipp/avr-eclipse-fork/updatesite/](https://raw.githubusercontent.com/mnlipp/avr-eclipse-fork/updatesite/) as URL.