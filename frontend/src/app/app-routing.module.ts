import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ApplicationListComponent } from './features/applications/components/application-list.component';
import { ApplicationDetailComponent } from './features/applications/components/application-detail.component';
import { ExtractionReviewComponent } from './features/extractions/components/extraction-review.component';

const routes: Routes = [
  { path: '', redirectTo: '/applications', pathMatch: 'full' },
  { path: 'applications', component: ApplicationListComponent },
  { path: 'applications/:id', component: ApplicationDetailComponent },
  { path: 'applications/:id/review/:documentId', component: ExtractionReviewComponent },
  { path: '**', redirectTo: '/applications' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
