This is a directory for all of the code running on the device.

# Firmware Module Configuration and Token File Guide

## Configuration File

The configuration file is located at:

```
/etc/sss_firmware/config.cfg
```

### Purpose:

This file contains key-value pairs and other settings necessary for the proper operation of the firmware module.

### Structure:

Below is a structure of the file with indices of lines and their purposes.
The file is a few lines with important data about current configuration.
Each line contains the described data and only that, no additional names or assignments.
The indices start from 0:

0. **[Is Configured]**  
   It is '0' or '1'. 0 means false, 1 means true.

1. **[Owner ID]**  
   Device owner's ID.

2. **[Owner email]**  
   Similar to Owner's ID. It exists to avoid fetching the email from the database if possible.

---

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
