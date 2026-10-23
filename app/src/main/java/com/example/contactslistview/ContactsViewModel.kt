package com.example.contactslistview

import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    val contacts = mutableListOf(
        "Ahmed Ben Ali",
        "Sarra Trabelsi",
        "Mohamed Gharbi",
        "Ines Jaziri",
        "Youssef Mansouri",
        "Amira Chaabane",
        "Karim Bouazizi",
        "Nour Hammami"
    )
}
