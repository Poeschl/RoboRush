<template>
  <BaseModal>
    <template #header>
      <h4 class="modal-card-title is-size-4">
        <FontAwesomeIcon icon="fa-regular fa-user" />
        User Details
      </h4>
    </template>

    <template #content>
      <div class="level">
        <div class="level-left">
          <div class="level-item is-block">
            <div class="is-size-5">Username</div>
            <div class="has-text-grey-light">This is also your robot name</div>
          </div>
        </div>
        <div class="level-right">
          <div class="level-item">
            {{ userStore.username }}
          </div>
        </div>
      </div>
      <div class="level">
        <div class="level-left">
          <div class="level-item is-size-5">API access token</div>
        </div>
        <div class="level-right">
          <div class="level-item">
            <CopyTextInput v-model:value="userStore.accessToken" />
          </div>
        </div>
      </div>
    </template>
    <template #footer>
      <button class="button" @click="$emit('close')">Close</button>
    </template>
  </BaseModal>
</template>

<script setup lang="ts">
import BaseModal from "@/components/templates/BaseModal.vue";
import { useUserStore } from "@/stores/UserStore";
import CopyTextInput from "@/components/form-components/CopyTextInput.vue";

const userStore = useUserStore();

const emit = defineEmits<{
  (e: "close"): void;
}>();
</script>

<style scoped></style>
