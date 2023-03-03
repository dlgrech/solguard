from flask import Flask

app = Flask(__name__)

@app.route('/config')
def home():
    return 'Hello, Config!'
