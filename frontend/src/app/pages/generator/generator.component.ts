import { Component } from '@angular/core';

@Component({
  selector: 'app-generator',
  templateUrl: './generator.component.html',
  styleUrls: ['./generator.component.css']
})
export class GeneratorComponent {
  length = 12;
  count = 3;
  includeUppercase = true;
  includeLowercase = true;
  includeNumbers = true;
  includeSpecial = true;
  generated: string[] = [];
  error = '';

  generate(): void {
    const pools: string[] = [];
    if (this.includeUppercase) pools.push('ABCDEFGHIJKLMNOPQRSTUVWXYZ');
    if (this.includeLowercase) pools.push('abcdefghijklmnopqrstuvwxyz');
    if (this.includeNumbers) pools.push('0123456789');
    if (this.includeSpecial) pools.push('!@#$%^&*()_+-=[]{}|;:,.<>?');

    if (!pools.length) {
      this.error = 'Select at least one character set.';
      this.generated = [];
      return;
    }
    if (this.length < 8 || this.length > 64) {
      this.error = 'Password length must be between 8 and 64.';
      this.generated = [];
      return;
    }
    if (this.count < 1 || this.count > 20) {
      this.error = 'Number of passwords must be between 1 and 20.';
      this.generated = [];
      return;
    }

    const fullPool = pools.join('');
    this.generated = Array.from({ length: this.count }, () => this.makePassword(this.length, fullPool));
    this.error = '';
  }

  private makePassword(length: number, pool: string): string {
    let out = '';
    for (let i = 0; i < length; i++) {
      out += pool.charAt(Math.floor(Math.random() * pool.length));
    }
    return out;
  }
}
