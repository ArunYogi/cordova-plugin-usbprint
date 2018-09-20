# cordova-plugin-usbprint
This Plugin allows you to print documents via USB printer.


## Supported Platform
* Android

## Usage

### Installation

```
cordova plugin add cordova-plugin-usbprint --save
```
Once application was started "UsbPrint" will be available in "window.cordova.plugins". So you can access plugin as
```
window['cordova'].plugins.UsbPrinter....()
```
### Methods

* getConnectedPrinters(successCallback, failureCallback) :=> Gets all connected USB printer.
* connect(printername, successCallback, failureCallback) :=> Connect or gets permission for the mentioned printer identified via "printername". "printername" can be identified from the printers list got from above method call.
* disconnect(printername, successCallback, failureCallback) :=> Disconnect or gets permission for the mentioned printer identified via "printername". "printername" can be identified from the printers list got from above method call.
* print(printername, msg, successCallback, failureCallback) :=> Get all connected USB printer.
* isPaperAvailable(printername, successCallback, failureCallback) :=> Tells whether paper is available in mentioned printer.
* sendCommand(printername, command, successCallback, failureCallback) :=> Sends POS command to the printer device.
* cutPaper(printername, successCallback, failureCallback) :=> Trigger Full cut paper event to printer.


#### Step 1:
First scan for all connected printers via USB
```
            window['cordova'].plugins.UsbPrinter.getConnectedPrinters((result) => {
                console.log(result);
                // result will be list of printers connected to the usb device
                // success callback execution
            }, err => {
                console.error(err)
                // failure callback execution
            });
```

#### Step 2:
Connect or get permission for the printer from app. Connect to the printer using printer_name.
```
            window['cordova'].plugins.UsbPrinter.connect(printer.printername, (result) => {
                console.log(result)
                // success callback execution
            }, err => {
                // use this method to recognise the disconnection of usb device
                console.error(err)
                // failure callback execution
            });
```
Note: When USB gets disconnected from the mobile device, then failure callback is invoked. So use this to reecognise the unsuccesful connection and disconnection from mobile device.

#### Step 3:
Send string to print.
```
            window['cordova'].plugins.UsbPrinter.print(printer.printername, message, (result) => {
                console.log("result of usb print action", result)
                // successful callback execution
            }, err => {
                console.error('Error in usb print action', err)
                // failure callback execution
            });
```
