class ProductModel:
    def __init__(self, name, durability, manufacture_date, category):
        # We will automatically generate the new id
        self.id = 0
        self.name = name
        self.durability = durability
        self.manufacture_date = manufacture_date
        self.category = category
        self.available_count = 0
        self.is_material_scarce = False
