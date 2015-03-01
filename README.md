# avr-eclipse-fork
A fork of avr-eclipse.

This fork currently merges in its master the master from avr-eclipse with R2.4.2 from avr-eclipse. 
You could say that this is the "better master" for the project.

This fork includes the good avrdude front-end from R2.4.2. without any changes. It adds the debug 
launch configurations from master that have been left out in R2.4.2. These launch configurations have
been rebased on the now available CDT "GDB Hardware Debugging" backend. The benefit of having these launch 
configurations is that the chosen gdbserver is started as part of the launch and that the dialogs focus
on the options available for the AVR microcontrollers. In addition to that, the launch configurations
include a work around for a long standing gdb bug that prevents the disassemly window from working properly.

The tycho/maven files have been updated with the files from R2.4.2, but they are untested. Personally, 
I think that maven is the worst thing that has ever happened to Java development environments and I don't
use it and won't use it. For building the p2 repository, simply go to the feature project and use
the export wizard (or click on "Build" in the update site project).

The "test plugins" have also not been tested. I have no idea what they actually do and whether they have
ever worked properly.
