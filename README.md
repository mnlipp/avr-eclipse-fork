avr-eclipse-plugin
==================


The AVR Plugin has no "master" project. It is a collection of projects,
some of them containing just source code, some of them only relevant for
building the plugin. The XML files within these projects determine their
function and are responsible for the integration of the different parts
into Eclipse.

But debugging the plugin is easy: just right click on any project,
e.g. de.innot.avreclipse.core, and select "Debug as > Eclipse Application".
A new instance of Eclipse is started which will include all of the projects
in the AVR Plugin. You can set breakpoints and debug to your hearts
content.  Please be aware, that the original Eclipse instance, the one
containing the source code of the plugin, should not have the AVR Plugin
installed as this may lead to conflicts between an installed plugin and the
plugin under test (this problem can of course be configured away, but it is
tedious)


Building the plugin for redistribution is a different procedure, actually
separate from Eclipse itself. I have taken this approach because it is more
robust and predictable than the built-in Eclipse tools.

To build the plugin you need Maven. Once you have Maven installed you can
open a Command Shell and change to the "de.innot.avreclipse-releng"
directory. Then you just need to run "mvn clean install" and the plugin
will be build, including running all tests. The first run will probably
take just about forever, because maven will download a large number of
required tools and libraries, including the core parts of Eclipse
itself. But this stuff is cached, so further builds should be quicker (they
are still slow, but hey, building is not something needed every few
minutes).

If the build is successful the finished plugin will be located in the
"de.innot.avreclipse-p2repository" project. A refresh of this project may
be necessary to make the new files visible in Eclipse.

Please be aware that the "master" branch of the Git repo may not build. I
haven't done builds for quite some time so things are in a bit of disarray.
However the "R2.4.0" branch should build successfully and is probably the
best starting point for working on the plugin.

Cheers,

Thomas
