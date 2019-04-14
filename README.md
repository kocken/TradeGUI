# TradeGUI
A trading bot JavaFX GUI for clean & easy monitoring of profits, assets and log messages. 

![Alt text](TradeGUI.gif?raw=true "Preview")

This application was built as a hobby project to test out JavaFX and potentially to use this GUI as a base for a trading bot which I might develop in the future.

There's inbuilt support for changing currency to display balance/profit/trades in different currencies. 
Note that the currency values which are used for conversion are currently hardcoded (in the Types/Currency enum) for demo-purposes and should preferably be grabbed in realtime.
Also note that the currency-swap logic might require overlooking depending on situation. Currently the currency is swapped for the new one, whereas in many scenarios the user might just want to change the display currency.

Thanks to [RichTextFX](https://github.com/FXMisc/RichTextFX) which is used in this application for the nice CSS-styled text-area.
