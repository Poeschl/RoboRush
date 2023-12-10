<template>
  <div
    ref="toast"
    class="notification"
    :class="{ autoclose: autoClose, 'is-success': type == ToastType.SUCCESS, 'is-danger': type == ToastType.ERROR, 'is-info': type == ToastType.INFO }"
  >
    <button class="delete" v-if="!autoClose" @click="close()"></button>
    {{ message }}
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ToastType } from "@/models/ToastType";

// ms the modal will stay on auto close
const autoCloseTimeout = 5000;

const toast = ref<HTMLDivElement>();

const props = withDefaults(
  defineProps<{
    type: ToastType;
    autoClose?: boolean;
    message: string;
  }>(),
  {
    autoClose: true,
  },
);

const emits = defineEmits<{
  (e: "close"): void;
}>();

onMounted(() => {
  if (props.autoClose) {
    setTimeout(() => close(), autoCloseTimeout);
  }
});

const close = () => {
  if (toast.value != undefined) {
    const elem = toast.value;
    elem.classList.add("fade-out");
    elem.onanimationend = () => {
      emits("close");
    };
  }
};
</script>

<style scoped lang="scss">
$right-margin: 1rem;

.notification {
  position: fixed;
  z-index: 100;
  top: 5rem;
  right: $right-margin;
  text-align: center;
  animation: fadein 0.5s;

  &.fade-out {
    animation: fadeout 0.5s;
  }

  &.autoclose {
    padding: 1.25rem 1.5rem;
  }
}

@keyframes fadein {
  from {
    right: 0;
    opacity: 0;
  }
  to {
    right: $right-margin;
    opacity: 1;
  }
}

@keyframes fadeout {
  from {
    right: $right-margin;
    opacity: 1;
  }
  to {
    right: 0;
    opacity: 0;
  }
}
</style>
