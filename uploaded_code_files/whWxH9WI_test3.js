// jDKqs58L_test3.js

// Hardcoded secret
const API_KEY = "supersecret123";
const SECRET_KEY = "myhardcodedsecret123";
const SECRET = "hardcoded";


// Insecure eval
eval("console.log('Injected code: ' + userInput)");

// XSS via innerHTML
let element = document.getElementById("demo");
element.innerHTML = userInput;

// Weak crypto
const crypto = require("crypto");
const md5hash = crypto.createHash("md5").update("password").digest("hex");

// Unsafe file operations
const fs = require("fs");
fs.readFile("user_input.txt", (err, data) => {
  if (err) throw err;
  console.log(data.toString());
});
