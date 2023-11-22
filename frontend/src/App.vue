<template>
  <div>
    <NavBar class="mb-5"/>
    <main class="container">
      <router-view/>
    </main>
  </div>
</template>

<script setup lang="ts">
console.info(`Swagger UI: ${window.location.origin}/rest/swagger-ui`);
</script>
