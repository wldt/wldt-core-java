# 🤖 White Label Digital Twin (WLDT) Framework

```text
A Digital Twin (DT) is a comprehensive software representation of an individual physical asset (PA). 
It includes the properties, conditions, relationships and behavior(s) of the real-life object through models and data. 
A Digital Twin is a set of realistic models that can digitalize an object’s behavior in the deployed environment. 
The Digital Twin represents and reflects its physical twin and remains its virtual counterpart across the object’s entire lifecycle.
```

☕️ The White Label Digital Twin (WLDT) library aims to support the design, development, and deployment of Digital Twins within the Internet of Things (IoT) ecosystems.
The library has been designed following the latest DT definitions coming from both Industrial and Scientific domains and identifying DTs as active, flexible and scalable software components.

⚖️ Dual Licensing: The WLDT library is released under a dual licensing model: GPL-3.0 for non-commercial, research, academic, and open-source use, 
and a Commercial License for any commercial use or proprietary deployment.

The complete Documentation of the Library is available at the following link: [https://wldt.github.io/](https://wldt.github.io/)

---

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

---

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
- 📝 [Logging in WLDT](docs/wldt_logger.md): Describes the logging system of the WLDT library, including how to configure, customize and use it.

---

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

```
@INPROCEEDINGS{PICONE2025DCOSS,
  author={Picone, Marco and Martinelli, Matteo and Burattini, Samuele and Giulianelli, Andrea and Ricci, Alessandro},
  booktitle={2025 21st International Conference on Distributed Computing in Smart Systems and the Internet of Things (DCOSS-IoT)}, 
  title={The Two Faces of Interoperability: Bridging Cyber and Physical Spaces with Digital Twins}, 
  year={2025},
  volume={},
  number={},
  pages={1-8},
  keywords={Adaptation models;Technological innovation;Protocols;Software architecture;Soft sensors;Semantics;Smart systems;Digital twins;Interoperability;Standards;Digital Twins;Industrial Internet of Things;Cyber-Physical Systems;Interoperability},
  doi={10.1109/DCOSS-IoT65416.2025.00078}}
```

---

## 📄 License

This project is released under a dual licensing model:

- **GNU General Public License v3 (GPL-3.0)** for non-commercial, research, academic, and open-source use.
- **Commercial License** required for any commercial use, proprietary products, or if you do not intend to release your source code.

This license applies to all forms of the software, including source code and compiled/binary forms.

**Core and Plugins:**

The WLDT Core is required for all plugins to function. While plugins may have separate licenses determined by their authors or contributors, they cannot operate independently without the Core.

Therefore, any use, distribution, or modification of plugins that depend on the Core is subject to the Core's dual licensing model:

- **Non-commercial, research, academic, or open-source use:** The Core and any dependent plugins may be used under the GPL-3.0, in combination with each plugin's individual license.
- **Commercial use, proprietary products, or cases where the source code is not released:** A separate Commercial License must be obtained for the Core, which also governs the use of any dependent plugins.

In summary, the WLDT Core’s dual licensing terms **apply to the Core and any plugins that rely on it**, while plugins may retain their own licenses for standalone components or other aspects. When used together with the Core, the final application must comply with the WLDT dual licensing terms.

For full WLDT license terms, see the [`LICENSE`](./LICENSE) file.

To obtain a Commercial License or for any licensing inquiries, please contact the author Marco Picone through one of the following addresses:

- picone.m@gmail.com
- wldtdev@gmail.com

---

## ℹ️ Disclaimer

This software is provided "as is", without warranty of any kind, express or implied, 
including but not limited to warranties of merchantability, fitness for a particular purpose, or non-infringement.

The authors and copyright holders are not liable for any claims, 
damages, or other liabilities arising from the use of this software.

This is a research/experimental project. It is **not intended as a commercial product** and 
comes with no guarantee of stability, maintenance, or support.

---

## 📧 Contacts

For any questions, requests for specific developments, or collaboration opportunities, feel free to contact us at: wldtdev [at] gmail [dot] com.
You may also reach out directly to the *Creator* and *Main Contributor*, Dr. Marco Picone (picone.m [at] gmail [dot] com).
Additional information is available on the [WLDT GitHub Page](https://wldt.github.io/) and on my official [website](https://www.marcopicone.net/).

---

## 🙌 Contributors

This project is made possible thanks to the contributions of the following people:

- **Marco Picone** – University of Modena & Reggio Emilia, Italy – Founder, Author & Main Contributor
- **Samuele Burattini** – University of Bologna, Italy – Key Contributor
- **Marta Spadoni** – University of Bologna, Italy – Master Thesis 2022 – Additional Contributor
