from flask import Flask
from flask import jsonify

app = Flask(__name__)

@app.route('/config')
def config():
    return jsonify(
        test1="123",
        test2=123
    )
