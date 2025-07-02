# 🤖 WhiteLabel Digital Twin Framework

```text
A Digital Twin (DT) is a comprehensive software representation of an individual physical asset (PA). 
It includes the properties, conditions, relationships and behavior(s) of the real-life object through models and data. 
A Digital Twin is a set of realistic models that can digitalize an object’s behavior in the deployed environment. 
The Digital Twin represents and reflects its physical twin and remains its virtual counterpart across the object’s entire lifecycle.
```

☕️ The White Label Digital Twin (WLDT) library aims to support the design, development, and deployment of Digital Twins within the Internet of Things (IoT) ecosystems.
The library has been designed following the latest DT definitions coming from both Industrial and Scientific domains and identifying DTs as active, flexible and scalable software components.

The complete Documentation of the Library is available at the following link: [https://wldt.github.io/](https://wldt.github.io/)

## 📦 Library Dependency Import

🔗 The official library repository is available at the following link [https://central.sonatype.com/artifact/io.github.wldt/wldt-core/](https://central.sonatype.com/artifact/io.github.wldt/wldt-core/)

🛠 For Maven projects you can import the WLDT Library into your ``<dependencies></dependencies>`` tag using the following snippet:

```xml
<dependency>
    <groupId>io.github.wldt</groupId>
    <artifactId>wldt-core</artifactId>
    <version>0.5.0</version>
</dependency>
```

If you are using Gradle use instead the following: 

```groovy
implementation group: 'io.github.wldt', name: 'wldt-core', version: '0.5.0'
```

## 📖 Documentation & References

With the aim to support readability of the documentation and to provide a better understanding of the library's functionalities,
the documentation is structured in the following sections through independent pages:

- 📌 [DT Definition & Basic Concepts](docs/dt_definition_basic_concepts.md): Provides the definition of a Digital Twin and the basic concepts 
that led the design and development of the WLDT library.
- 🏛 [Library Structure](docs/library_structure.md): Provides the details about the library structure and the main components that make up the architecture of WLDT.
- 🚀 [Getting Started Tutorial](docs/getting_started.md): A step-by-step guide to help you get started with the WLDT library, including how to create your first Digital Twin.
- 🔗 [Relationships Management](docs/relationship_management.md): Explains how to manage relationships existing in the physical layer and how to model them in the Digital Twin in terms of declaration and updates.
- 🔌 [Configurable Adapters](docs/configurable_adapters.md): Describes the concept of Configurable Adapters, which allows developers to create reusable and customizable adapters for different protocols and data formats.
- 🗄 [Storage & Query System](docs/storage_query_system.md): Explains the storage and query system of the WLDT library, detailing how to store and retrieve data from the Digital Twin.
- ️️⚙️ [Resources & Management Interface](docs/resources_management_interface.md): Provides the details about 
the management interface of the WLDT library and how to use it to handle the manageable DT's resource through its life cycle.

## 📚 Scientitic Citation & Reference

If you use the WLDT Library in a Scientific Paper please use this reference:

```
@article{PICONE2021100661,
    title = {WLDT: A general purpose library to build IoT digital twins},
    journal = {SoftwareX},
    volume = {13},
    pages = {100661},
    year = {2021},
    issn = {2352-7110},
    doi = {https://doi.org/10.1016/j.softx.2021.100661},
    url = {https://www.sciencedirect.com/science/article/pii/S2352711021000066},
    author = {Marco Picone and Marco Mamei and Franco Zambonelli},
    keywords = {Internet of Things, Digital twin, Library, Software agent}
}
```

TODO ... ADD DCOSS Paper

```
@article{PICONE2025DCOSS,
  ...
}
```

## ℹ️ Disclaimer

This software is provided "as is", without warranty of any kind, express or implied, 
including but not limited to the warranties of
merchantability, fitness for a particular purpose and noninfringement. 
In no event shall the authors or copyright holders be liable for any
claim, damages or other liability, whether in an action of contract,
tort or otherwise, arising from, out of or in connection with the
software or the use or other dealings in the software.

This is a research / experimental project. It is not intended as a
commercial product and comes with no guarantee of stability,
maintenance or support.

For any questions, specific developments or collaborations,
you may contact us at: wldtdev [at] gmail [dot] com

## 📧 Contacts

For any questions, requests for specific developments, or collaboration opportunities, feel free to contact us at: wldtdev [at] gmail [dot] com.
You may also reach out directly to the *Creator* and *Main Contributor*, Dr. Marco Picone (picone.m [at] gmail [dot] com).
Additional information is available on the [WLDT GitHub Page](https://wldt.github.io/) and on my official [website](https://www.marcopicone.net/).
