<template>
  <div class="container">
    <v-row justify="center" align="center">
      <v-col></v-col>
      <v-col>
        <h2>Tournaments Dashboard</h2>
      </v-col>
      <v-col>
        <v-card
          width="135"
          color="white"
          height="50"
          outlined
          raised
          class="px-3 mx-5 switch-private"
          data-cy="privacySwitch"
        >
          <v-switch
            v-model="privacySetting"
            class="ma-2"
            :label="privacySetting ? 'Public' : 'Private'"
            @change="changePrivacySetting()"
          ></v-switch>
        </v-card>
      </v-col>
    </v-row>
    <div class="stats-container">
      <div
        class="items"
        style="background-color: rgba(250,180,30,0.85); color: white"
      >
        <div class="icon-wrapper" ref="totalFirstPlace">
          <animated-number data-cy="first" :number="stats.totalFirstPlace" />
        </div>
        <div class="project-name">
          <p>1st Places</p>
        </div>
      </div>
      <div
        class="items"
        style="background-color: rgba(127,127,119,0.85); color: white"
      >
        <div class="icon-wrapper" ref="totalSecondPlace">
          <animated-number data-cy="second" :number="stats.totalSecondPlace" />
        </div>
        <div class="project-name">
          <p>2nd Places</p>
        </div>
      </div>
      <div
        class="items"
        style="background-color: rgba(205,126,66,0.85); color: white"
      >
        <div class="icon-wrapper" ref="totalThirdPlace">
          <animated-number data-cy="third" :number="stats.totalThirdPlace" />
        </div>
        <div class="project-name">
          <p>3rd Places</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalPerfect">
          <animated-number data-cy="perfect" :number="stats.totalPerfect" />
        </div>
        <div class="project-name">
          <p>Perfect Scores</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalSolved">
          <animated-number data-cy="solved" :number="stats.totalSolved" />
        </div>
        <div class="project-name">
          <p>Total Solved</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalUnsolved">
          <animated-number data-cy="unsolved" :number="stats.totalUnsolved" />
        </div>
        <div class="project-name">
          <p>Total Unsolved</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalCorrectAnswers">
          <animated-number
            data-cy="correct"
            :number="stats.totalCorrectAnswers"
          />
        </div>
        <div class="project-name">
          <p>Correct Answers</p>
        </div>
      </div>
      <div
        class="items"
        style="background-color: rgba(88,170,58,0.85); color: white"
      >
        <div class="icon-wrapper" ref="score">
          <animated-number data-cy="score" :number="stats.score"
            >pt</animated-number
          >
        </div>
        <div class="project-name">
          <p>Score</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="totalWrongAnswers">
          <animated-number data-cy="wrong" :number="stats.totalWrongAnswers" />
        </div>
        <div class="project-name">
          <p>Wrong Answers</p>
        </div>
      </div>
    </div>
    <ul>
      <li class="list-header ">
        <div class="col">Title</div>
        <div class="col">Creator</div>
        <div class="col">Conclusion</div>
        <div class="col">Result</div>
        <div class="col">Rank</div>
        <div class="col last-col"></div>
      </li>
      <li
        data-cy="quiz"
        class="list-row"
        v-for="tourn in stats.closedTournaments"
        :key="tourn.id"
        @click="showResults(tourn.solvedQuiz)"
      >
        <div class="col">
          {{ tourn.title }}
        </div>
        <div class="col">
          {{ tourn.creator.username }}
        </div>
        <div class="col">
          {{ tourn.conclusionDate }}
        </div>
        <div class="col">
          {{ calculateResult(tourn.solvedQuiz) }}
        </div>
        <div class="col">
          {{ calculateRank(tourn) }}
        </div>
        <div class="col last-col">
          <v-icon color="primary" dense>fas fa-chevron-circle-right</v-icon>
        </div>
      </li>
    </ul>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { TournamentDashStats } from '@/models/management/TournamentDashStats';
import AnimatedNumber from '@/components/AnimatedNumber.vue';
import SolvedQuiz from '@/models/statement/SolvedQuiz';
import StatementManager from '@/models/statement/StatementManager';
import { ClosedTournament } from '@/models/management/ClosedTournament';

@Component({
  components: { AnimatedNumber }
})
export default class TournamentsTabView extends Vue {
  stats: TournamentDashStats = new TournamentDashStats();
  privacySetting: boolean = false;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.stats = await RemoteServices.getTournamentsDashboardStats();
      this.privacySetting = !this.stats.anonimize;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  calculateResult(quiz: SolvedQuiz) {
    let correct = 0;
    for (let i = 0; i < quiz.statementQuiz.questions.length; i++) {
      if (
        quiz.statementQuiz.answers[i] &&
        quiz.correctAnswers[i].correctOptionId ===
          quiz.statementQuiz.answers[i].optionId
      ) {
        correct += 1;
      }
    }
    return `${correct}/${quiz.statementQuiz.questions.length}`;
  }

  calculateRank(tourn: ClosedTournament) {
    if (tourn.ranking == 0) return '--';
    let tag = 'th';
    if (tourn.ranking % 10 == 1 && tourn.ranking != 11) tag = 'st';
    if (tourn.ranking % 10 == 2 && tourn.ranking != 12) tag = 'nd';
    if (tourn.ranking % 10 == 2 && tourn.ranking != 13) tag = 'rd';
    return tourn.ranking + tag;
  }

  async showResults(quiz: SolvedQuiz) {
    let statementManager: StatementManager = StatementManager.getInstance;
    statementManager.correctAnswers = quiz.correctAnswers;
    statementManager.statementQuiz = quiz.statementQuiz;
    await this.$router.push({ name: 'quiz-results' });
  }

  async changePrivacySetting() {
    try {
      await RemoteServices.changeTournamentStatsPrivacy();
      this.stats.anonimize = this.privacySetting;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
}
</script>

<style lang="scss" scoped>
.stats-container {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
  justify-content: center;
  align-items: stretch;
  align-content: center;
  height: 100%;

  .items {
    background-color: rgba(250, 255, 255, 0.75);
    color: #1976d2;
    border-radius: 5px;
    flex-basis: 25%;
    margin: 20px;
    cursor: pointer;
    transition: all 0.6s;
  }
}

.icon-wrapper,
.project-name {
  display: flex;
  align-items: center;
  justify-content: center;
}

.icon-wrapper {
  font-size: 100px;
  transform: translateY(0px);
  transition: all 0.6s;
}

.icon-wrapper {
  align-self: end;
}

.project-name {
  align-self: start;
}
.project-name p {
  font-size: 24px;
  font-weight: bold;
  letter-spacing: 2px;
  transform: translateY(0px);
  transition: all 0.5s;
}

.items:hover {
  & .project-name p {
    transform: translateY(-10px);
  }
  & .icon-wrapper i {
    transform: translateY(5px);
  }
}

h2 {
  font-size: 26px;
  margin: 20px 0;
  text-align: center;
  small {
    font-size: 0.5em;
  }
}

ul {
  overflow: hidden;
  padding: 0 5px;

  li {
    border-radius: 3px;
    padding: 5px 10px;
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .list-header {
    background-color: #1976d2;
    color: white;
    font-size: 14px;
    text-transform: uppercase;
    letter-spacing: 0.03em;
    text-align: center;
  }

  .col {
    flex-basis: 25% !important;
    margin: auto; /* Important */
    text-align: center;
  }

  .list-row {
    background-color: #ffffff;
    box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
    display: flex;
    cursor: pointer;
    transition: background-color 100ms;
  }

  .last-col {
    flex-basis: 10% !important;
  }

  .list-row:hover {
    background-color: #deedf8;
  }

  .switch-private {
    display: inline-block;
  }
}
</style>
