def prettyPrint(x){
    return groovy.json.JsonOutput.prettyPrint(groovy.json.JsonOutput.toJson(x));
}