<template>
    <div class="container">
        <h2>Available Tournaments</h2>

    </div>
</template>

<script lang="ts">
  import { Component, Vue } from 'vue-property-decorator';
  import RemoteServices from '@/services/RemoteServices';
  import StatementManager from '@/models/statement/StatementManager';
  import StatementQuiz from '@/models/statement/StatementQuiz';
  import StatementQuestion from '@/models/statement/StatementQuestion';
  import StatementAnswer from '@/models/statement/StatementAnswer';
  import { Tournament } from '@/models/management/Tournament';
  import Topic from '@/models/management/Topic';

  @Component
  export default class OpenTournamentsView extends Vue {
    tournaments: Tournament[] = [];

    async created() {
      await this.$store.dispatch('loading');
      try {
        this.tournaments = (await RemoteServices.getOpenTournaments()).reverse();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
      await this.$store.dispatch('clearLoading');
    }
  }
</script>

<style lang="scss" scoped>

</style>