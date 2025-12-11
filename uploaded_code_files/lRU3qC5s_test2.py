import os
import hashlib
import sqlite3

user_input = "1 OR 1=1"
conn = sqlite3.connect(":memory:")
cursor = conn.cursor()

# SQLi
cursor.execute("SELECT * FROM users WHERE id = " + user_input)

# Command injection
os.system("ls " + user_input)

# Hardcoded secret
password = "MyPassword123"

# Unsafe eval
eval("print('hacked!')")

# Weak crypto
hashlib.md5(b"data")

# Path traversal (unsafe file open)
open("../../etc/passwd", "r")
