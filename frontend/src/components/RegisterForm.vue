<template>
  <BaseModal>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-solid fa-user-plus" />
        Registration
      </h4>
    </template>

    <template #content>
      <form>
        <TextInput
          v-model:value="registerRequest.username"
          label="Username"
          placeholder="Username"
          help="Your username on the website and also the name of your robot. Must be longer then 3 letters."
          autocomplete="username"
          :max-length="255"
          :validate="
            (value: string) => {
              return value.length > 3;
            }
          "
        />
        <TextInput
          v-model:value="registerRequest.password"
          label="Password"
          help="The password must have a length of 8 chars."
          placeholder="New Password"
          autocomplete="new-password"
          :is-password="true"
          :validate="
            (value: string) => {
              return value.length >= 8;
            }
          "
        /><TextInput
          v-model:value="registerRequest.passwordRepeat"
          label="Password (Repeat)"
          help="Please repeat your password this once. (We promise)"
          placeholder="Repeat Password"
          autocomplete="new-password"
          :is-password="true"
          :validate="
            (value: string) => {
              return value.length >= 8 && value == registerRequest.password;
            }
          "
          @keydown.enter="$emit('register', registerRequest)"
        />
      </form>
    </template>
    <template #footer>
      <button class="button is-success" @click="$emit('register', registerRequest)" :class="{ 'is-loading': loading }" :disabled="!valid">Register</button>
      <button class="button" @click="$emit('close')">Cancel</button>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from "@/components/templates/BaseModal.vue";
import TextInput from "@/components/form-components/TextInput.vue";
import type { LoginRequest, RegisterRequest } from "@/models/User";
import { computed, ref } from "vue";

const registerRequest = ref<RegisterRequest>({ username: "", password: "", passwordRepeat: "" });

const valid = computed<boolean>(() => {
  const request = registerRequest.value;
  return request.username.length > 3 && request.password.length >= 8 && request.passwordRepeat == request.password;
});

const props = defineProps<{
  loading: boolean;
}>();

const emit = defineEmits<{
  (e: "close"): void;
  (e: "register", val: RegisterRequest): void;
}>();
</script>

<style scoped></style>
