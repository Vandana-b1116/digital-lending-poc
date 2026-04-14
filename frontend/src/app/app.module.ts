import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { StoreModule } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { applicationsReducer } from './store/applications/applications.reducer';
import { extractionsReducer } from './store/extractions/extractions.reducer';
import { ApplicationsEffects } from './store/applications/applications.effects';
import { ExtractionsEffects } from './store/extractions/extractions.effects';
import { ApplicationListComponent } from './features/applications/components/application-list.component';
import { ApplicationDetailComponent } from './features/applications/components/application-detail.component';
import { DocumentUploadComponent } from './features/documents/components/document-upload.component';
import { ExtractionReviewComponent } from './features/extractions/components/extraction-review.component';
import { environment } from '../environments/environment';

@NgModule({
  declarations: [
    AppComponent,
    ApplicationListComponent,
    ApplicationDetailComponent,
    DocumentUploadComponent,
    ExtractionReviewComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    StoreModule.forRoot({
      applications: applicationsReducer,
      extractions: extractionsReducer,
    }),
    EffectsModule.forRoot([ApplicationsEffects, ExtractionsEffects]),
    StoreDevtoolsModule.instrument({
      maxAge: 25,
      logOnly: environment.production,
    }),
  ],
  bootstrap: [AppComponent],
})
export class AppModule {}
