<template>
  <div class="field">
    <label class="label">{{ props.label }}</label>
    <div class="control">
      <input
        class="input"
        type="text"
        :maxlength="props.maxLength"
        :placeholder="props.placeholder"
        :value="value"
        :class="{ 'is-success': valid }"
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

const props = defineProps<{
  label: string;
  placeholder: string;
  value: any;
  help: string;
  maxLength: number;
  validate: (value: string) => boolean;
}>();

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
