<!--
This is a abstract modal component which can be reused to display other modals.
It content is fully configurable via slots, so any content can be embedded.
There are 3 slots available for templating: `header`, `content` and `footer`.
-->

<template>
  <div class="modal is-active">
    <div class="modal-background" />
    <div class="modal-content">
      <div class="card">
        <div class="card-header">
          <slot name="header" />
        </div>

        <div class="card-content form">
          <slot name="content" />
        </div>
        <slot name="footer" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts"></script>
