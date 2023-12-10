<template>
  <div class="field">
    <label class="label">{{ props.label }}</label>
    <div class="control">
      <input
        class="input"
        :readonly="readonly"
        :type="props.isPassword ? 'password' : 'text'"
        :maxlength="props.maxLength"
        :placeholder="props.placeholder"
        :value="value"
        :class="{ 'is-success': valid }"
        :autocomplete="autocomplete"
        @input="updateValue(($event.target as HTMLInputElement).value)"
      />
    </div>
    <p v-if="props.help.length > 0" class="help">
      {{ props.help }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";

const props = withDefaults(
  defineProps<{
    label: string;
    placeholder?: string;
    value: any;
    help?: string;
    maxLength?: number;
    isPassword?: boolean;
    autocomplete?: string;
    readonly?: boolean;
    validate?: (value: string) => boolean;
  }>(),
  {
    placeholder: "",
    help: "",
    maxLength: undefined,
    isPassword: false,
    autocomplete: undefined,
    readonly: false,
    validate: () => true,
  },
);

const emit = defineEmits<{
  (e: "update:value", val: string): void;
}>();

const value = ref(props.value);

const valid = computed(() => {
  return props.validate(value.value);
});

const updateValue = (newValue: string) => {
  value.value = newValue;
  emit("update:value", newValue);
};
</script>

<style scoped></style>
