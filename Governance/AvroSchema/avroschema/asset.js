/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
asset.manager = function(ctx) {
    var tenantAPI = require('/modules/tenant-api.js').api;
    var rxtApp = require('rxt').app;
    var availableUIAssetTypes = rxtApp.getUIActivatedAssets(ctx.tenantId);
    var availableAssetTypes = rxtApp.getActivatedAssets(ctx.tenantId);
    var allAvailableAssetTypes = String(availableAssetTypes.concat(availableUIAssetTypes));

    var getDependentAseetAvroSchema = function (thisObj, genericArtifact, asset, userRegistry, recordName) {
        // print("Inside the get dependency method");
        var dependencies = asset.dependencies;
        if(dependencies.length > 0) {
            // There are dependecies
            for(var index = 0; index < dependencies.length; index++) {
                var dependentAsset = dependencies[index];
                var associatedName = String(dependentAsset["associationName"]);
                var associationType = String(dependentAsset["associationType"]);
                var associationUUID = String(dependentAsset["associationUUID"]);
                var associationPath = String(dependentAsset["associationPath"]);
                if(associationType == "avroschema" && associatedName == recordName) {
                    // print("A match of record name found");
                    // Need to check the version too
                    var dependentAssetObj = thisObj._super.get.call(thisObj, associationUUID);
                    // print(dependentAssetObj);
                    var rawArtifact = thisObj.am.manager.getGenericArtifact(associationUUID);
                    assetWithAttributes = setCustomAssetAttributes(thisObj, rawArtifact, dependentAssetObj, userRegistry);
                    return assetWithAttributes.avro_schema;
                }

                return null;
            }
        }

        return null;
    }

    var setCustomAssetAttributes = function (thisObj, genericArtifact, asset, userRegistry) {
        //Overview
        asset.overview_name = asset.attributes.overview_name;
        asset.overview_version = asset.attributes.overview_version;
        asset.overview_description = asset.attributes.overview_description;
        // print(asset);

        var avro_schema = {};
        avro_schema.namespace = asset.attributes.overview_namespace;
        avro_schema.name = asset.attributes.overview_name;
        avro_schema.type = "record";

        avro_schema.fields = [];

        if(asset.attributes.recordFields_Name != null) {
            for(var record_index = 0; record_index < asset.attributes.recordFields_Name.length; record_index++) {
                field_entry = {};
                
                if (asset.attributes.recordFields_Name.constructor === Array) {
                    var name = asset.attributes.recordFields_Name[record_index];
                    var type = asset.attributes.recordFields_Type[record_index];
                    var default_value = asset.attributes.recordFields_Default[record_index];
                    var name_str = String(name);
                    var type_str = String(type);
                    var default_value_str = String(default_value);
                    field_entry.name = name_str;
                    field_entry.default = default_value_str;

                    if(type_str == "arrayobject") {
                        // print("Looks like array object type reached");
                        // print(name_str);
                        // print(asset.attributes.arrayObjectType_Name);

                        if(asset.attributes.arrayObjectType_Name.constructor === Array) {
                            // arrayObjectType_Name doesn't seem to have strings. So making it.
                            arrayObjectTypeNames = [];
                            asset.attributes.arrayObjectType_Name.forEach(function(item) {
                                arrayObjectTypeNames.push(String(item));
                            });
                            var array_index = arrayObjectTypeNames.indexOf(name_str);
                            // print(typeof(array_index));
                            // print(array_index);

                            if(array_index != -1) {
                                // print(array_index);
                                var arrayObjectDefault = null;
                                arrayDefaultObject = asset.attributes.arrayObjectType_Default[array_index];
                                if(arrayDefaultObject != null) {
                                    arrayObjectDefault = String(arrayDefaultObject);
                                    // print(arrayObjectDefault);
                                }
                                
                                var isArrayObject = String(asset.attributes.arrayObjectType_arrayObject[array_index]);
                                var array_object_type = String(asset.attributes.arrayObjectType_itemType[array_index]);

                                
                                var recordName = asset.attributes.arrayObjectType_recordName[array_index];
                                recordName = (record_index != null) ? String(recordName) : recordName;

                                // print("ALL SET TO CHECK ARRAY TYPE");

                                if(isArrayObject == "Yes") {
                                    // print("isArrayObjuect turns out to be Yes");
                                    var array_type_entry = [];
                                    if(arrayObjectDefault != null) {
                                        // print("Pushing array object default");
                                        array_type_entry.push(arrayObjectDefault);
                                    }
                                    if(array_object_type == "string") {
                                        array_type_entry.push(array_object_type);
                                        // print("assigning array entry type");
                                        field_entry.type = array_type_entry;
                                    } else if(array_object_type == "record") {
                                        // print("Trying to get associations");
                                        setDependencies(genericArtifact, asset, userRegistry);
                                        // print("ASSOCIATIONS_1");

                                        dependencyAssetAvroSchema = getDependentAseetAvroSchema(thisObj, genericArtifact, asset, userRegistry, recordName);
                                        // print("Pushing the dependency schema");
                                        if(dependencyAssetAvroSchema != null) {
                                            array_type_entry.push(JSON.parse(dependencyAssetAvroSchema));
                                        }
                                        // print("Assigning to array entry type");
                                        field_entry.type = array_type_entry;
                                    }
                                } else {
                                    // print("This is No");
                                    var json_type_entry = {};
                                    json_type_entry.type = "array";
                                    if(array_object_type == "string") {
                                        // print("Type is string thus assigning@@@@@@@");
                                        json_type_entry.items = "string";
                                        field_entry.type = json_type_entry;
                                    } else if(array_object_type == "record") {
                                        // print("Trying to get associations");
                                        setDependencies(genericArtifact, asset, userRegistry);
                                        // print("ASSOCIATIONS_2");
                                        dependencyAssetAvroSchema = getDependentAseetAvroSchema(thisObj, genericArtifact, asset, userRegistry, recordName);
                                        if(dependencyAssetAvroSchema != null) {
                                            json_type_entry.items = JSON.parse(dependencyAssetAvroSchema);
                                        }
                                    }
                                }
                            }
                        } else {
                            if(asset.attributes.arrayObjectType_Name == name_str) {
                                arrayDefaultObject = asset.attributes.arrayObjectType_Default;
                                if(arrayDefaultObject != null) {
                                    arrayObjectDefault = String(arrayDefaultObject);
                                }
                                var isArrayObject = asset.attributes.arrayObjectType_arrayObject;
                                var array_object_type = asset.attributes.arrayObjectType_itemType;
                                var array_object_type = asset.attributes.arrayObjectType_recordName;
                                if(isArrayObject == "Yes") {
                                    var array_type_entry = [];
                                    if(arrayObjectDefault != null) {
                                        array_type_entry.push(arrayObjectDefault);
                                    }
                                    if(array_object_type == "string") {
                                        array_type_entry.push(array_object_type);
                                        field_entry.type = array_type_entry;
                                    } else if(array_object_type == "record") {
                                        // print("Trying to get associations");
                                        setDependencies(genericArtifact, asset, userRegistry);
                                        // print("ASSOCIATIONS_3");
                                        dependencyAssetAvroSchema = getDependentAseetAvroSchema(thisObj, genericArtifact, asset, userRegistry, recordName);
                                        if(dependencyAssetAvroSchema != null) {
                                            array_type_entry.push(JSON.parse(dependencyAssetAvroSchema));
                                        }
                                    }
                                } else {
                                    var json_type_entry = {};
                                    json_type_entry.type = "array";
                                    if(array_object_type == "string") {
                                        json_type_entry.items = "string";
                                        field_entry.type = json_type_entry;
                                    } else if(array_object_type == "record") {
                                        // print("Trying to get associations");
                                        setDependencies(genericArtifact, asset, userRegistry);
                                        // print("ASSOCIATIONS_4");
                                        dependencyAssetAvroSchema = getDependentAseetAvroSchema(thisObj, genericArtifact, asset, userRegistry, recordName);
                                        if(dependencyAssetAvroSchema != null) {
                                            json_type_entry.items = JSON.parse(dependencyAssetAvroSchema);
                                        }
                                        // print(asset.dependencies.associationUUID);
                                        // var dependentAsset = thisObj._super.get.call(thisObj, asset.dependencies.associationUUID);
                                        // print(dependentAsset);
                                    }
                                }
                            }
                        }

                    } else if(type_str == "enum") {
                        enumTypeJSON = {}
                        enumTypeJSON.type = "enum";
                        enumTypeJSON.name = name_str;
                        enumValues = [];
                        
                        if(asset.attributes.enumTypes_Symbols.constructor === Array) {
                            enumNames = [];
                            asset.attributes.enumTypes_Name.forEach(function(item){
                                enumNames.push(String(item));
                            });
                            var enum_index = enumNames.indexOf(name_str)
                            if(enum_index != -1) {
                                var states = String(asset.attributes.enumTypes_Symbols[enum_index]);
                                enumValues = states.split(",");
                                enumTypeJSON.symbols = enumValues;
                            }
                        } else {
                            // Only if you have a value
                            if(asset.attributes.enumTypes_Name == name_str) {
                                var states = asset.attributes.enumTypes_Symbols;
                                enumValues = states.split(",");
                                enumTypeJSON.symbols = enumValues;
                            }
                        }

                        field_entry.type = enumTypeJSON;
                    } else {
                        field_entry.type = type_str;
                    }

                } else {        // This should be the case of there's only one field. TODO - Please check
                    field_entry.name = asset.attributes.recordFields_Name;
                    field_entry.type = asset.attributes.recordFields_Type;
                }

                avro_schema.fields.push(field_entry);
            }
        }

        asset.avro_schema_object = avro_schema;
        // asset.avro_schema = avro_schema;
        // print(avro_schema);

        try{
            // print("Converting to JSON string");
            // var avro_schema_str = JSON.stringify(decycle(avro_schema));
            var avro_schema_str = JSON.stringify(avro_schema);
            // print("Conversion BEFORE successful------------------------------");
            // print(avro_schema_str);
            // print("Conversion AFTER successful------------------------------");
            asset.avro_schema = avro_schema_str;
         
          } catch(e) {
            // print("######## BIG ERROR #######");
            // print(e);
            // print("######## BIG ERROR #######");
         }

        //  print("Converting to JSON string");
        //  // var avro_schema_str = JSON.stringify(decycle(avro_schema));
        //  var avro_schema_str = JSON.stringify(avro_schema);
        //  print("Conversion BEFORE successful------------------------------");
        //  print(avro_schema_str);
        //  print("Conversion AFTER successful------------------------------");
        //  asset.avro_schema = avro_schema_str;

        return asset;
    }; 

    var setDependencies = function(genericArtifact, asset ,userRegistry) {
        // print("SET DEPENDECIES CALLED ]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]");
        var dependencyArtifacts = genericArtifact.getDependencies();
        asset.dependencies = getAssociations(dependencyArtifacts, userRegistry);
        // print(asset.dependencies);
    };

    var setDependents = function(genericArtifact, asset, userRegistry) {
        var dependentArtifacts = genericArtifact.getDependents();
        asset.dependents = getAssociations(dependentArtifacts, userRegistry);
    };

    var getRegistry = function(cSession) {
        var tenantDetails = tenantAPI.createTenantAwareAssetResources(cSession,{type:ctx.assetType});
        if((!tenantDetails)&&(!tenantDetails.am)) {
            log.error('The tenant-api was unable to create a registry instance by resolving tenant details');
            throw 'The tenant-api  was unable to create a registry instance by resolving tenant details';
        }
        return tenantDetails.am.registry;
    };

    var getInterfaceTypeContent = function (resource) {
        var ByteArrayInputStream = Packages.java.io.ByteArrayInputStream;
        var content = resource.getContent();
        var value = '' + new Stream(new ByteArrayInputStream(content));
        return value;
    };

    var getAssociations = function(genericArtifacts, userRegistry){
        //Array to store the association names.
        var associations = [];
        if (genericArtifacts != null) {
            for (var index in genericArtifacts) {
                var deps = {};
                var path = genericArtifacts[index].getPath();
                var resource = userRegistry.registry.get('/_system/governance' + path);
                var mediaType = resource.getMediaType();
                var name = genericArtifacts[index].getQName().getLocalPart();
                var govUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
                var keyName = govUtils.getArtifactConfigurationByMediaType(getRegistry(ctx.session).registry, mediaType).getKey();
                if (isDisabledAsset(keyName)) {
                    continue;
                }
                var subPaths = path.split('/');
                var associationUUID = resource.getUUID();
                // var versionAttribute = ctx.rxtManager.getVersionAttribute(keyName);
                // var associationVersion = genericArtifacts[index].getAttribute(versionAttribute);
                // // This is only for WSO2 OOTB artifacts which have correct storage path
                // if (!associationVersion && (subPaths.length - 2) > -1) {
                //     associationVersion = subPaths[subPaths.length - 2]
                // }
                deps.associationName = name;
                deps.associationType = keyName;
                deps.associationUUID = associationUUID;
                deps.associationPath = resource.getPath();
                // deps.associationVersion = associationVersion;
                associations.push(deps);
            }
        }
        return associations;
    };

    var isDisabledAsset = function (shortName) {
        // This method will return true if shortName not available in allAvailableAssetTypes string.
        var pat1 = new RegExp("^" + shortName + ",");
        var pat2 = new RegExp("," + shortName + "$");
        var pat3 = new RegExp("," + shortName + ",");
        return (!(pat3.test(allAvailableAssetTypes)) && !(pat1.test(allAvailableAssetTypes)) && !(pat2.test(allAvailableAssetTypes)));
    };

    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        get: function(id) {
            var asset = this._super.get.call(this, id);
            var userRegistry = getRegistry(ctx.session);
            //get the GenericArtifactManager
            var rawArtifact = this.am.manager.getGenericArtifact(id);

            try {
                setCustomAssetAttributes(this, rawArtifact, asset, userRegistry);
            } catch (e){}

            try {
                setDependencies(rawArtifact, asset, userRegistry);
            } catch (e){}
            try {
                setDependents(rawArtifact, asset, userRegistry);
            } catch (e){

            }

            return asset;
        }
    };
};

asset.configure = function () {
    return {
        meta: {
            ui: {
                icon: 'fw fw-rest-service',
                iconColor: 'purple'
            },
            isDependencyShown: true,
            isDiffViewShown:false
        }
    }
};

// asset.renderer = function(ctx){
//     return {
//         pageDecorators:{
//             downloadPopulator:function(page){
//                 //Populate the links for downloading content RXTs
//                 if(page.meta.pageName === 'details'){
//                     var config = require('/config/store.js').config();
//                     var pluralType = 'wadls'; //Assume it is a WADl
//                     var domain = require('carbon').server.tenantDomain({tenantId:ctx.tenantId});
//                     page.assets.downloadMetaData = {}; 
//                     page.assets.downloadMetaData.enabled = false;
//                     var dependencies = page.assets.dependencies || [];
//                     var downloadFile = dependencies.filter(function(item){
//                         return ((item.associationType == 'wadl')||(item.associationType == 'swagger'));
//                     })[0];
//                     if(downloadFile){
//                       var typeDetails = ctx.rxtManager.getRxtTypeDetails(downloadFile.associationType);
//                       page.assets.downloadMetaData.enabled = true;  
//                       page.assets.downloadMetaData.downloadFileType = typeDetails.singularLabel.toUpperCase();
//                       pluralType = typeDetails.pluralLabel.toLowerCase();
//                       page.assets.downloadMetaData.url = config.server.https+'/governance/'+pluralType+'/'+downloadFile.associationUUID+'/content?tenant='+domain;          
//                       if(downloadFile.associationType == 'swagger'){
//                         page.assets.downloadMetaData.swaggerUrl = '/pages/swagger?path='+downloadFile.associationPath;
//                       }
//                     }
//                 }
//             }
//         }
//     };
// };
