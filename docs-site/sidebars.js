/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
    {
      type: "category",
      label: "Introduction",
      collapsed: false,
      items: ["index", "get-started"],
    },
    {
      type: "category",
      label: "Guides",
      collapsed: false,
      items: [
        "guides/platform-support",
        "guides/image-sources",
        "guides/dominance-logic",
      ],
    },
    {
      type: "category",
      label: "API Reference",
      collapsed: false,
      items: ["api/check-face", "api/types"],
    },
    {
      type: "category",
      label: "Help",
      collapsed: false,
      items: ["troubleshooting", "examples"],
    },
  ],
};

module.exports = sidebars;
