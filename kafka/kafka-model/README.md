# Apache Avro

## Qu'est-ce qu'Apache Avro ?

Apache Avro est un **système de sérialisation de données** utilisé pour stocker ou transmettre des données de manière compacte et efficace.  
Il combine **un format de données** (binaire ou JSON) avec **un schéma** qui décrit la structure des données.

Les principaux avantages sont :
- **Format compact** : les données binaires sont plus petites que JSON ou XML.
- **Interopérabilité** : compatible avec Java, Python, C++, etc.
- **Évolutivité des schémas** : possibilité de modifier les champs sans casser les consommateurs.
- **Intégration avec Kafka** : souvent utilisé pour échanger des messages structurés dans les pipelines de données.

---

## Concepts clés

- **Record** : une structure équivalente à un objet avec des champs nommés.
- **Field** : chaque champ du record, avec un type (`int`, `string`, `boolean`, etc.).
- **Schema** : description JSON de la structure des données. Peut évoluer avec des versions.
- **Serialization / Deserialization** : conversion entre objet en mémoire et données binaires ou JSON.

---

## Exemple de schéma Avro

```json
{
  "type": "record",
  "name": "User",
  "fields": [
    {"name": "id", "type": "int"},
    {"name": "name", "type": "string"},
    {"name": "email", "type": ["null", "string"], "default": null}
  ]
}
