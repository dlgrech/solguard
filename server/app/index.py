from flask import Flask

app = Flask(__name__)

@app.route('/config')
def config():
    return 'Hello, Config!'
