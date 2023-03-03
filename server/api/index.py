from flask import Flask, request, jsonify, url_for

app = Flask(__name__)

class Charity:
    def __init__(self, id, name, url, image_url, short_desc, long_desc,public_key):
        self.id = id
        self.name = name
        self.url = url
        self.image_url = image_url
        self.short_desc = short_desc
        self.long_desc = long_desc
        self.public_key = public_key

    def to_json(self):
        return vars(self)


@app.route('/config')
def config():
    malaria_consortium = Charity(
        id = "malaria_consortium",
        name="Malaria Consotium",
        url = "https://www.malariaconsortium.org",
        image_url = url_for("static", filename="img/charities/charity_logo_malaria_consortium.png", _external=True),
        short_desc = "Medicine to prevent malaria",
        long_desc = """Malaria kills over 600,000 people annually, mostly children under 5 in sub-Saharan Africa. 

        Seasonal malaria chemoprevention is preventive medicine that saves children’s lives. 

        It is given during the four months of the year when malaria infection rates are especially high.""",
        public_key = "FmG96N2aPZEh9iiTuuzsUKxoKXwTtN7g62gSRVyssgxN"

    )

    against_malaria = Charity(
        id = "against_malaria_foundation",
        name="Against Malaria Foundation",
        url = "https://www.againstmalaria.com",
        image_url = url_for("static", filename="img/charities/charity_logo_against_malaria.jpeg", _external=True),
        short_desc = "Nets to prevent malaria",
        long_desc = """Malaria kills over 600,000 people annually, mostly children under 5 in sub-Saharan Africa.

        Nets save lives.

        Recipients of nets hang and sleep under them so they are not bitten by malaria-carrying mosquitoes.""",
        public_key = "4ecHnFdKzxyCXrcCTSumPiZvNgoc1kFu736cgYptCQDP"
    )

    helen_keller_international = Charity(
        id = "helen_keller_international",
        name="Helen Keller International",
        url = "https://helenkellerintl.org",
        image_url = url_for("static", filename="img/charities/charity_logo_hellen_keller_international.png", _external=True),
        short_desc = "Supplements to prevent vitamin A deficiency",
        long_desc = """Vitamin A deficiency leaves children vulnerable to infections and can lead to death. 

        Over 200,000 children's deaths to vitamin A deficiency each year.

        This program saves lives by providing vitamin A supplements to children under 5 years old.""",
        public_key = "Fb8C9MxhFGziU8KvZAqbyuAZhrnv6bYK7HHUxGwKfM5P"
    )

    new_incentives = Charity(
        id = "new_incentives",
        name="New Incentives",
        url = "https://www.newincentives.org",
        image_url = url_for("static", filename="img/charities/charity_logo_new_incentives.png", _external=True),
        short_desc = "Cash incentives for routine childhood vaccines",
        long_desc = """In Nigeria, 43% of infants did not receive all recommended childhood vaccines in 2019. 

        This program provides cash transfers to incentivize caregivers to bring babies to clinics for routine childhood vaccinations, which prevent disease and reduce child mortality. 

        It operates in North West Nigeria.""",
        public_key = "D78kGkxJWa1th75zEuZYTswAtJuUtPNMRyByEZrYC9rm"
    )


    charities = [malaria_consortium, against_malaria, helen_keller_international, new_incentives]

    return jsonify(
        charities=[c.to_json() for c in charities],
        identity_uri=request.url_root,
        identity_icon_uri=url_for("static", filename="img/solguard_brand_icon.png")
    )
