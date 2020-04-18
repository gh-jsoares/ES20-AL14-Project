<template>
  <v-card class="table">
    <h1>StudentQuestions</h1>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component
export default class StudentQuestionsView extends Vue {}
</script>

<style lang="scss" scoped></style>
