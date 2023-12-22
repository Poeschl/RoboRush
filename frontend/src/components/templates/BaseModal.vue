<!--
This is a abstract modal component which can be reused to display other modals.
It content is fully configurable via slots, so any content can be embedded.
There are 3 slots available for templating: `header`, `content` and `footer`.
-->

<template>
  <div class="modal is-active">
    <div class="modal-background" />
    <div class="modal-card">
      <div class="modal-card-head">
        <slot name="header" />
      </div>

      <div class="modal-card-body">
        <slot name="content" />
      </div>
      <div class="modal-card-foot is-justify-content-end">
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>
