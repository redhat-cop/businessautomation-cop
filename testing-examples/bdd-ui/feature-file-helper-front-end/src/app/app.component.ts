import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'feature-tool';

  navLinks = [
    { label: 'Create', path: "/home" },
    { label: 'Edit', path: "/openFile" },
  ];
  activeLink = this.navLinks[0];
}
