import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  template: `
    <nav class="navbar">
      <span class="navbar-brand">Digital Lending — Loan Officer Portal</span>
      <a routerLink="/applications" class="nav-link">Applications</a>
    </nav>
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .navbar {
      background: #1a237e;
      color: white;
      padding: 12px 24px;
      display: flex;
      align-items: center;
      gap: 24px;
    }
    .navbar-brand { font-size: 1.1rem; font-weight: 600; }
    .nav-link { color: #90caf9; text-decoration: none; }
    .nav-link:hover { text-decoration: underline; }
    .main-content { padding: 24px; }
  `],
})
export class AppComponent {
  title = 'digital-lending-frontend';
}
