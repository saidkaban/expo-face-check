// @ts-check
const { themes: prismThemes } = require("prism-react-renderer");

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: "expo-face-check",
  tagline:
    "On-device face detection for Expo apps. Detect faces locally. No API keys, no cloud, just native intelligence.",
  favicon: "img/favicon.ico",

  url: "https://expo-face-check.com",
  baseUrl: "/",

  organizationName: "saidkaban",
  projectName: "expo-face-check",

  onBrokenLinks: "throw",
  onBrokenMarkdownLinks: "warn",

  i18n: {
    defaultLocale: "en",
    locales: ["en"],
  },

  presets: [
    [
      "classic",
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          routeBasePath: "/",
          sidebarPath: "./sidebars.js",
          editUrl:
            "https://github.com/saidkaban/expo-face-check/tree/main/docs-site/",
        },
        blog: false,
        theme: {
          customCss: "./src/css/custom.css",
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      colorMode: {
        defaultMode: "light",
        disableSwitch: false,
        respectPrefersColorScheme: true,
      },
      navbar: {
        title: "expo-face-check",
        logo: {
          alt: "expo-face-check Logo",
          src: "img/logo.svg",
        },
        items: [
          {
            href: "https://github.com/saidkaban/expo-face-check",
            position: "right",
            className: "header-github-link",
            "aria-label": "GitHub repository",
          },
        ],
      },
      footer: {
        style: "light",
        copyright: `Copyright © ${new Date().getFullYear()} expo-face-check. MIT License.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ["bash", "json", "kotlin", "swift"],
      },
    }),
};

module.exports = config;
