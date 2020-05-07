<template>
  <div class="container">
    <h2>
      Student Questions Dashboard
      <v-card
        color="white"
        height="50"
        outlined
        raised
        class="px-3 mx-5 switch-private"
      >
        <v-switch
          v-model="visibilitySetting"
          class="ma-2"
          :label="visibilitySetting ? 'Public' : 'Private'"
          @change="toggleVisibilitySetting"
        />
      </v-card>
    </h2>
    <div v-if="stats != null" class="stats-container">
      <div class="items">
        <div class="icon-wrapper" ref="rejected">
          <animated-number data-cy="rejected" :number="stats.rejected" />
        </div>
        <div class="project-name">
          <p>Rejected student questions</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="approved">
          <animated-number data-cy="approved" :number="stats.approved" />
        </div>
        <div class="project-name">
          <p>Approved student questions</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="total">
          <animated-number data-cy="total" :number="stats.total" />
        </div>
        <div class="project-name">
          <p>Total submitted student questions</p>
        </div>
      </div>
      <div class="items">
        <div class="icon-wrapper" ref="percentage">
          <animated-number data-cy="percentage" :number="stats.percentage">
            %
          </animated-number>
        </div>
        <div class="project-name">
          <p>Percentage of approved student questions</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import StudentQuestionStats from '@/models/user/dashboard/StudentQuestionStats';
import RemoteServices from '@/services/RemoteServices';
import AnimatedNumber from '@/components/AnimatedNumber.vue';

@Component({
  components: { AnimatedNumber }
})
export default class StudentQuestionStatsView extends Vue {
  stats: StudentQuestionStats | null = null;
  visibilitySetting: Boolean = true;

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.stats = await RemoteServices.getStudentQuestionStats();
      this.visibilitySetting = this.stats.visibilitySetting;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async toggleVisibilitySetting() {
    await this.$store.dispatch('loading');
    try {
      this.visibilitySetting = await RemoteServices.toggleStudentQuestionStatsVisibility();
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
    background-color: rgba(255, 255, 255, 0.75);
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
  border: 3px solid black;

  & .project-name p {
    transform: translateY(-10px);
  }
  & .icon-wrapper i {
    transform: translateY(5px);
  }
}

.switch-private {
  display: inline-block;
}
</style>
