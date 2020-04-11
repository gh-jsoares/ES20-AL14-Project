<template>
  <div class="container">
    <h2>Create Tournament</h2>
  </div>
</template>

<script>
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import StatementManager from '@/models/statement/StatementManager';
import StatementQuiz from '@/models/statement/StatementQuiz';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StatementAnswer from '@/models/statement/StatementAnswer';
import { Tournament } from '@/models/management/Tournament';
import Topic from '@/models/management/Topic';

@Component
export default class CreateTournamentView extends Vue {
  async created() {
    // TODO
  }
}
</script>

<style scoped>

</style>