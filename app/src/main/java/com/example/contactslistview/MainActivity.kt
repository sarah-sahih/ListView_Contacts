
package com.example.contactslistview

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var contacts: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var etContact: EditText
    private lateinit var btnAjouter: Button
    private lateinit var btnModifier: Button
    private lateinit var lvContacts: ListView

    private var positionSelectionnee = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Récupérer la liste conservée dans le ViewModel
        val viewModel = ViewModelProvider(this)[ContactsViewModel::class.java]
        contacts = viewModel.contacts

        etContact = findViewById(R.id.etContact)
        btnAjouter = findViewById(R.id.btnAjouter)
        btnModifier = findViewById(R.id.btnModifier)
        lvContacts = findViewById(R.id.lvContacts)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            contacts
        )

        lvContacts.adapter = adapter

        // Sélectionner un contact
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.setText(contacts[position])
            etContact.setSelection(etContact.text.length)
            etContact.requestFocus()
        }

        // Supprimer un contact avec un appui long
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Supprimer le contact")
                .setMessage("Voulez-vous vraiment supprimer ${contacts[position]} ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    contacts.removeAt(position)

                    if (positionSelectionnee == position) {
                        positionSelectionnee = -1
                        etContact.text.clear()
                        etContact.error = null
                    } else if (positionSelectionnee > position) {
                        positionSelectionnee--
                    }

                    adapter.notifyDataSetChanged()
                    lvContacts.clearChoices()
                    lvContacts.requestLayout()

                    Toast.makeText(
                        this,
                        "Contact supprimé",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Annuler", null)
                .show()
            true
        }

        // Ajouter un contact
        btnAjouter.setOnClickListener {
            ajouterContact()
        }

        // Modifier un contact
        btnModifier.setOnClickListener {
            modifierContact()
        }

        // Modifier avec la touche Done du clavier
        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                modifierContact()

                val clavier = getSystemService(
                    Context.INPUT_METHOD_SERVICE
                ) as InputMethodManager

                clavier.hideSoftInputFromWindow(etContact.windowToken, 0)
                etContact.clearFocus()
                lvContacts.clearChoices()
                lvContacts.requestLayout()

                true
            } else {
                false
            }
        }
    }

    // Ajouter un contact
    private fun ajouterContact() {
        val nouveauNom = etContact.text.toString().trim()

        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Refuser les doublons
        if (contacts.any { it.equals(nouveauNom, ignoreCase = true) }) {
            etContact.error = "Ce contact existe déjà"
            return
        }

        contacts.add(nouveauNom)
        contacts.sortBy { it.lowercase() }

        adapter.notifyDataSetChanged()
        etContact.text.clear()
        etContact.error = null
        positionSelectionnee = -1
        lvContacts.clearChoices()
        lvContacts.requestLayout()

        Toast.makeText(
            this,
            "Contact ajouté : $nouveauNom",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Modifier un contact
    private fun modifierContact() {
        if (positionSelectionnee == -1) {
            Toast.makeText(
                this,
                R.string.msg_aucune_selection,
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nouveauNom = etContact.text.toString().trim()

        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Vérifier si un autre contact possède déjà ce nom
        val doublon = contacts.withIndex().any { (index, nom) ->
            index != positionSelectionnee &&
                    nom.equals(nouveauNom, ignoreCase = true)
        }

        if (doublon) {
            etContact.error = "Ce contact existe déjà"
            return
        }

        contacts[positionSelectionnee] = nouveauNom
        contacts.sortBy { it.lowercase() }

        // Retrouver la position du contact après le tri
        positionSelectionnee = contacts.indexOfFirst {
            it.equals(nouveauNom, ignoreCase = true)
        }

        adapter.notifyDataSetChanged()
        lvContacts.clearChoices()

        if (positionSelectionnee != -1) {
            lvContacts.setItemChecked(positionSelectionnee, true)
        }

        Toast.makeText(
            this,
            "Contact modifié : $nouveauNom",
            Toast.LENGTH_SHORT
        ).show()
    }
}
