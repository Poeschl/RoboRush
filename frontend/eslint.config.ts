/* eslint-env node */

import pluginVue from "eslint-plugin-vue";
import eslint from "@eslint/js";
import { defineConfigWithVueTs, vueTsConfigs } from "@vue/eslint-config-typescript";

export default defineConfigWithVueTs(
  eslint.configs.recommended,
  vueTsConfigs.recommendedTypeChecked,
  ...pluginVue.configs["flat/recommended"],
  {
    languageOptions: {
      ecmaVersion: "latest",
    },
  },
  {
    rules: {
      "vue/html-indent": ["off"],
      "vue/attributes-order": ["off"],
      "vue/max-attributes-per-line": ["off"],
      "@typescript-eslint/no-floating-promises": ["off"],
      "vue/html-closing-bracket-spacing": ["off"],
      "vue/attribute-hyphenation": ["error", "never"],
      "vue/v-on-event-hyphenation": ["error", "never"],
    },
  },
);
