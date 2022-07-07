package de.living.model

class BigUser(
    var email: String = "",
    var name: String = "",
    var uid: String = "",
    var groupNames: ArrayList<String> = arrayListOf(),
    var memberPerGroup: HashMap<String, ArrayList<String>> = hashMapOf(),
    var tasksPerGroup: HashMap<String, ArrayList<HashMap<String, String>>> = hashMapOf()

)