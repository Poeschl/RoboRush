<template>
  <BaseModal>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-arrow-right-to-bracket" />
        Login
      </h4>
    </template>

    <template #content>
      <form>
        <TextInput
          v-model:value="loginRequest.username"
          label="Username"
          placeholder="Username"
          autocomplete="username"
          :max-length="255"
          :validate="
            (value) => {
              return value.length > 3;
            }
          "
        />
        <TextInput
          v-model:value="loginRequest.password"
          label="Password"
          placeholder="Password"
          autocomplete="current-password"
          :is-password="true"
          :validate="
            (value) => {
              return value.length > 8;
            }
          "
          @keydown.enter="$emit('login', loginRequest)"
        />
      </form>
    </template>
    <template #footer>
      <button class="button is-success" @click="$emit('login', loginRequest)" :class="{ 'is-loading': loading }" :disabled="!valid">Login</button>
      <button class="button" @click="$emit('close')">Cancel</button>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from "@/components/templates/BaseModal.vue";
import TextInput from "@/components/form-components/TextInput.vue";
import type { LoginRequest } from "@/models/User";
import { computed, ref } from "vue";

const loginRequest = ref<LoginRequest>({ username: "", password: "" });

const valid = computed<boolean>(() => {
  const request = loginRequest.value;
  return request.username.length > 3 && request.password.length >= 8;
});

const props = defineProps<{
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "login", val: LoginRequest): void;
}>();
</script>

<style scoped></style>
