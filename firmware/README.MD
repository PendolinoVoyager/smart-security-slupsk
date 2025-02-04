This is a directory for all of the code running on the device.

# Firmware Module Configuration and Token File Guide

## Configuration File

The configuration files are located at:

```
/etc/sss_firmware/*
```
## Email file

### Purpose:

*/etc/sss_firmware/email.txt*

This file contains current owner's email. The absence of any contents in the file means that the device isn't configured. 

### Structure:

Is empty or contains a single line with user's email. 

## Token File

The token file is located at:

```
/etc/sss_firmware/token.txt
```

### Purpose:

The token file is used for secure authentication with external services or APIs.

### Structure:

The file should contain:

1. **API Token**: A single line with the API token or secret key.
