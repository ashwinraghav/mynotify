RNotify: Monitoring file system events across NFS
==================================================

Table of Contents
-----------------

*Description
*Authors
*Documentation
*Installation
*Configuration
*Notes
*Example
*References

Description
-----------

iNotify is a kernel-level API that allows user applications to monitor file system events. An application is able to use iNotify and register a file or directory. It will receive prompt notifications from iNotify of changes made including creation, deletion, and modification. This allows the application to deal with changes to a relavent file or directory.

Because iNotify works with the local VFS, it does not currently support NFS file/directory notifications. This is because if a remote computer changes a file in NFS directory, those changes are not made through the local VFS, and thus iNotify is not made aware of the event.

RNotify functions as an out-of-band extension to iNotify. By using a watcher server application on the NFS server, the client application is made aware of file system events on the NFS server.

Authors
-------

Ashwin Raghav
Tom Tracy II

Documentation
-------------

Under Construction

Installation
------------

Under Construction

Configuration
-------------

Under Construction

Notes
-----

Under Construction

Example Usage
-------------

Under Construction

References
----------

http://linux.die.net/man/7/inotify
