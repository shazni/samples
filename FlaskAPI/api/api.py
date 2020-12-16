from flask import Flask, request
from flask_restful import abort, Api, fields, marshal_with, reqparse, Resource
from datetime import datetime
from models import ProductModel
import status, socket
from pytz import utc

# import time
# import redis

# cache = redis.Redis(host='redis', port=6379)
# cache.flushdb()

class ProductManager():
	last_id = 0

	def __init__(self):
		self.products = {}

	def insert_message(self, product):
		self.__class__.last_id += 1
		product.id = self.__class__.last_id
		self.products[self.__class__.last_id] = product
		# cache.incr('product_count')

	def get_product(self, id):
		return self.products[id]

	def delete_product(self, id):
		del self.products[id]
		# cache.decr('product_count')

product_fields = {
	'id': fields.Integer,
	'uri': fields.Url('product_endpoint'),
	'name': fields.String,
	'durability': fields.Integer,
	'manufacture_date': fields.DateTime,
	'category': fields.String,
	'available_count': fields.Integer,
	'is_material_scarce': fields.Boolean
}

product_manager = ProductManager()

# def get_product_count():
#     retries = 5
#     while True:
#         try:
#             return cache.get('product_count')
#         except redis.exceptions.ConnectionError as exc:
#             if retries == 0:
#                 raise exc
#             retries -= 1
#             time.sleep(0.5)

class Company(Resource):
	def get(self):
		count = 1
		# count = get_product_count()
		return 'Company ProductMasters has {} products'.format(count)

class Product(Resource):
	def abort_if_product_doesnt_exist(self, id):
		if id not in product_manager.products:
			abort(
			status.HTTP_404_NOT_FOUND,
			message="Product {0} doesn't exist".format(id))

	@marshal_with(product_fields)
	def get(self, id):
		self.abort_if_product_doesnt_exist(id)
		return product_manager.get_product(id)

	def delete(self, id):
		self.abort_if_product_doesnt_exist(id)
		product_manager.delete_product(id)
		return '', status.HTTP_204_NO_CONTENT

	@marshal_with(product_fields)
	def patch(self, id):
		self.abort_if_product_doesnt_exist(id)
		product = product_manager.get_product(id)
		parser = reqparse.RequestParser()
		parser.add_argument('name', type=str)
		parser.add_argument('durability', type=int)
		parser.add_argument('available_count', type=int)
		parser.add_argument('is_material_scarce', type=bool)

		args = parser.parse_args()

		if 'name' in args:
			product.name = args['name']
		if 'durability' in args:
			product.durability = args['durability']
		if 'available_count' in args:
			product.available_count = args['available_count']
		if 'is_material_scarce' in args:
			product.is_material_scarce = args['is_material_scarce']
		
		return product

class ProductList(Resource):
	@marshal_with(product_fields)
	def get(self):
		return [v for v in product_manager.products.values()]

	@marshal_with(product_fields)
	def post(self):
		print("Post request received");
		print(self)
		print(dir(self))
		print(dir(request))
		print("------------------------")
		print(request.url)
		print(request.headers)
		print(request.get_data())
		print(request.json)
		print(request.content_type)
		print(request.content_length)
		request_dict = request.get_json()
		print(request_dict)
		parser = reqparse.RequestParser()
		parser.add_argument('name', type=str, required=True, help='Name cannot be blank!')
		parser.add_argument('durability', type=int, required=True, help='Durability cannot be blank!')
		parser.add_argument('category', type=str, required=True, help='Product category cannot be blank!')
		print("parser arguments added")
		args = parser.parse_args()
		print(args)
		product = ProductModel(
			name=args['name'],
			durability=args['durability'],
			manufacture_date=datetime.now(utc),
			category=args['category']
		)

		product_manager.insert_message(product)
		return product, status.HTTP_201_CREATED

class Host(Resource):
	def get(self):
		return 'Hostname = {}'.format(socket.gethostname())

app = Flask(__name__)
api = Api(app)
api.add_resource(ProductList, '/api/products/')
api.add_resource(Company, '/api/company')
api.add_resource(Product, '/api/products/<int:id>', endpoint='product_endpoint')
api.add_resource(Host, '/api/host')

if __name__ == '__main__':
	app.run(host='0.0.0.0', debug=True)
