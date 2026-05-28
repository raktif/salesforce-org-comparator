# Salesforce Org Comparator

A Java desktop application for comparing metadata and records between two different Salesforce organizations.

## Features

- **SObject Field Comparison**: Checks fields, data types, and compliance between both Orgs
- **Metadata Comparison**: Supports comparison of:
  - Page Layouts
  - Validation Rules
  - Flows (Record Triggered)
  - Apex Triggers
  - Custom Metadata
  - Custom Settings
  - Permission Sets
  - Profiles
  - Groups
  - Approval Processes
  - Named Credentials

- **User-Friendly GUI**: Intuitive forms for entering connection data
- **Detailed Report**: Tables with comparison results
- **CSV Export**: Export results for further analysis

## Requirements

- Java 21 or higher
- Access to the Salesforce REST API on both Orgs
- OAuth2 authentication credentials or username/password with security token

## Installation

1. Navigate to the project folder:
   ```
   cd C:\SalesforceComparator
   ```

2. Run the build script:
   ```
   build.bat
   ```

   Or on Linux/Mac:
   ```
   bash build.sh
   ```

## How to Use

1. Run the application:
   ```
   run.bat
   ```
   
   Or alternatively:
   ```
   java -cp "build;lib\*" com.sfcomparator.ui.MainWindow
   ```

2. **Configure Organization 1 (Source)**:
   - Instance URL: Access URL for Org 1 (e.g., https://login.salesforce.com)
   - Username: Org 1 username
   - Password: Org 1 password
   - Security Token: Security token (if required)
   - Client ID and Client Secret: For OAuth2 authentication (optional)

3. **Configure Organization 2 (Target)**:
   - Repeat the previous steps with Org 2 credentials

4. **Configure Comparison**:
   - SObject Name: Name of the object to compare (e.g., Account, Contact)
   - Record Count Limit: Maximum number of records to check
   - Record Type: Specific record type (optional)
   - Select desired metadata via checkboxes

5. **Start Comparison**:
   - Click the "Start Comparison" button
   - Results will appear in the "Results" tab

6. **Export Results**:
   - Click "Export to CSV" to save the results

## Project Structure

```
SalesforceComparator/
├── src/main/java/com/sfcomparator/
│   ├── api/              # Salesforce API client
│   ├── model/            # Data models
│   ├── ui/               # GUI (Swing)
│   └── comparator/       # Comparison engine
├── lib/                  # External dependencies
├── build/                # Compiled files
├── dist/                 # Executable JAR
├── build.bat             # Build script (Windows)
├── build.sh              # Build script (Linux/Mac)
├── run.bat               # Run script (Windows)
└── README.md             # This file
```

## Dependencies

- `json-20231013.jar`: JSON processing library (already included in `lib/`)

## Salesforce Authentication

The application supports two authentication methods:

### 1. Username/Password with Security Token
- Username: Org username
- Password: Org password
- Security Token: Obtained from Setup > Personal Setup > Reset Your Security Token

### 2. OAuth2
- Client ID: Obtained from Setup > Apps > App Manager > Connected App
- Client Secret: Obtained from the same Connected App

## Important Notes

- The application was built using only the standard Java 21 APIs and does not require installing external dependencies (except for the provided JSON library)
- Authentication data is used only for the current session and is not stored on disk
- It is recommended to use a technical user with adequate permissions on both Orgs
- Comparison may take a few minutes depending on the amount of metadata

## Troubleshooting

### Authentication Error
- Verify that the credentials are correct
- Confirm that the Security Token was correctly appended to the password
- Validate the Instance URL (must be `https://login.salesforce.com` or `https://test.salesforce.com`)

### Compilation Error
- Confirm that Java 21 is installed: `java -version`
- Check the JAVA_HOME path in the build.bat script

### Connection Failure
- Check internet connectivity
- Confirm firewall permissions
- Validate that the REST API is enabled on the Org

## Future Development

- [ ] Full metadata comparison support
- [ ] Automatic change synchronization
- [ ] Comparison history
- [ ] Git integration for change tracking
- [ ] Web interface
- [ ] Automatic deployment support

## License

Proprietary - Internal use only

## Support

To report bugs or suggest improvements, contact the development team.
